//
//  QCBrokerPlugIn.h
//  QCBroker
//
//  Created by Celso Martinho on 4/5/09.
//  Copyright (c) 2009 SAPO. All rights reserved.
//

#import <Quartz/Quartz.h>

@interface QCBrokerPlugIn : QCPlugIn
{
	NSMutableArray *lineBuffer;
	BOOL isAlive;
	BOOL isReady;
	NSString *brokerServer;
	NSInteger brokerPort;
	NSString *brokerTopic;
}

@property(assign) NSString* inputServer;
@property(assign) NSString* inputPort;
@property(assign) NSString* inputTopic;
@property(assign) NSString* outputString;


/*
Declare here the Obj-C 2.0 properties to be used as input and output ports for the plug-in e.g.
@property double inputFoo;
@property(assign) NSString* outputBar;
You can access their values in the appropriate plug-in methods using self.inputFoo or self.inputBar
*/
-(void)listen;

@end
