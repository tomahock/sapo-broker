//
//  QCBrokerPlugIn.m
//  QCBroker
//
//  Created by Celso Martinho.
//  Copyright (c) 2010 SAPO. All rights reserved.
//

/* It's highly recommended to use CGL macros instead of changing the current context for plug-ins that perform OpenGL rendering */
#import <OpenGL/CGLMacro.h>

#import "QCBrokerPlugIn.h"
#import "sapo_broker.h"
#import "sapo_broker_xml.h"

#define	kQCPlugIn_Name				@"SAPOBroker"
#define	kQCPlugIn_Description		@"This patch will connect Quartz Composer with a SAPO Broker instance and output the subscribed topic as normal events"
#define	BUFFER_SIZE	1024

@implementation QCBrokerPlugIn

@dynamic inputServer, inputPort, inputTopic, outputString;

+ (NSDictionary*) attributes
{
	return [NSDictionary dictionaryWithObjectsAndKeys:kQCPlugIn_Name, QCPlugInAttributeNameKey, kQCPlugIn_Description, QCPlugInAttributeDescriptionKey, nil];
}

+ (NSDictionary*) attributesForPropertyPortWithKey:(NSString*)key
{

  if([key isEqualToString:@"inputTopic"])
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"Topic", QCPortAttributeNameKey,
            @"/sapo/fortune",  QCPortAttributeDefaultValueKey,
            nil];
  if([key isEqualToString:@"inputServer"])
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"Server", QCPortAttributeNameKey,
            @"broker.bk.sapo.pt",  QCPortAttributeDefaultValueKey,
            nil];
  if([key isEqualToString:@"inputPort"])
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"Port", QCPortAttributeNameKey,
            @"3322",  QCPortAttributeDefaultValueKey,
            nil];
  if([key isEqualToString:@"outputString"])
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"String", QCPortAttributeNameKey,
            nil];
	return nil;
}

+ (QCPlugInExecutionMode) executionMode
{

	return kQCPlugInExecutionModeProcessor;
}

+ (QCPlugInTimeMode) timeMode
{

	return kQCPlugInTimeModeIdle;
}

-(void)listen
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	int err=FALSE;

	SAPO_BROKER_T *sb;
	BrokerMessage *m;

  if(!isReady) return;
  
	isAlive=NO;
		
	NSLog(@"Connecting to %s to port %d to topic %s\n",[brokerServer UTF8String],brokerPort,[brokerTopic UTF8String]);
	
	sb = sb_new((char *)[brokerServer UTF8String], (int)brokerPort, SB_TYPE_TCP);

	if (!sb) err=TRUE;
	if (err==FALSE && (sb_connect(sb) != SB_OK)) err=TRUE;
	if (err==FALSE && (sb_subscribe(sb, EQUEUE_TOPIC, [brokerTopic UTF8String]) != SB_OK)) err=TRUE;

	isAlive= YES;
	
	if(err==FALSE) {
		while ((m = sb_receive(sb)) != NULL) {
			// NSLog(@"%s\n", m->payload);	
			/* if buffer is too full, ignore */
			if([lineBuffer count]<100) {
			//	NSLog(@"addObject\n");
				[lineBuffer addObject:[NSString stringWithCString:m->payload encoding:NSUTF8StringEncoding]];
				}
			//NSLog(@"sb_free_message %d %@\n",[lineBuffer count],[NSThread currentThread]);
			sb_free_message(m);
			}
		// NSLog(@"While ended\n");

		}
		else {
			NSLog(@"%s\n", sb_error());
	}
	
	NSLog(@"Freeing broker thread\n");
	if(sb) sb_disconnect(sb);
	isAlive=NO;
	isReady=NO;
	[pool release];
	return;
}

- (id) init
{
	if(self = [super init]) {
		lineBuffer= [[NSMutableArray array] retain];
	}
	
	return self;
}

- (void) finalize
{

	[super finalize];
}

- (void) dealloc
{
	[lineBuffer release];
	[super dealloc];
}

+ (NSArray*) plugInKeys
{
	return nil;
}

- (id) serializedValueForKey:(NSString*)key;
{
	return [super serializedValueForKey:key];
}

- (void) setSerializedValue:(id)serializedValue forKey:(NSString*)key
{
	[super setSerializedValue:serializedValue forKey:key];
}

@end

@implementation QCBrokerPlugIn (Execution)

- (BOOL) startExecution:(id<QCPlugInContext>)context
{
	isAlive=NO;
	isReady=NO;
	return YES;
}

- (void) enableExecution:(id<QCPlugInContext>)context
{

}

- (BOOL) execute:(id<QCPlugInContext>)context atTime:(NSTimeInterval)time withArguments:(NSDictionary*)arguments
{

  if(isReady==NO) {
    brokerServer=[NSString stringWithString:self.inputServer];
    brokerPort=[self.inputPort intValue];
    brokerTopic=[NSString stringWithString:self.inputTopic];
    [NSThread detachNewThreadSelector:@selector(listen) toTarget:self withObject:nil];
    isReady=YES;
  }
  
	if (isAlive==YES && [lineBuffer count]>0) {
	//	NSLog([NSString stringWithFormat:@"%d",[lineBuffer count]]);
	//	self.outputString= [NSString stringWithFormat:@"%d",[lineBuffer count]];
		if([lineBuffer objectAtIndex:0]) {
			self.outputString= [[NSString stringWithString:[lineBuffer objectAtIndex:0]] retain];
		}
		[lineBuffer removeObjectAtIndex:0];
	}
  
  return YES;
}

- (void) disableExecution:(id<QCPlugInContext>)context
{
	NSLog(@"Execution stopped disableExecution()\n");
	isAlive= NO;
	isReady= NO;
}

- (void) stopExecution:(id<QCPlugInContext>)context
{
	[self disableExecution:context];
}

@end
