#ifdef __cplusplus
extern "C" {
#endif
    #include <stdio.h>
    #include <time.h>
    #include <strings.h>
    #include <string.h>
#ifdef __cplusplus
} /* closing brace for extern "C" */
#endif

#include "broker.pb.h"


#define DEST "/protobuf-cc/topic"
#define PAYLOAD "PAYLOAD SOU MESMO PARVO"


void create_publish( char *dest, char *payload, unsigned len_payload, std::string *msg)
{
    sapo_broker::Atom *atom;
    sapo_broker::Atom::Action *action;
    sapo_broker::Atom::Publish *publish;
    sapo_broker::Atom_BrokerMessage *message;

    atom = new sapo_broker::Atom();
    action = atom->mutable_action();

    action->set_action_type( sapo_broker::Atom_Action_ActionType_PUBLISH );

    publish = action->mutable_publish();
    publish->set_destination( dest );
    publish->set_destination_type( sapo_broker::Atom_DestinationType_TOPIC );

    message = publish->mutable_message();
    message->set_payload( payload, len_payload );
    message->set_timestamp( time(NULL) );

    atom->SerializeToString( msg );
}


void extract_publish(std::string *msg, const char **payload, unsigned *len, const char **dest)
{
    sapo_broker::Atom atom;
    atom.ParseFromString( *msg );

    sapo_broker::Atom_Action  action = atom.action();
    if( action.action_type() == sapo_broker::Atom_Action_ActionType_PUBLISH )
        printf( "TYPE: OK\n" );
    else
        printf( "TYPE: BAD\n" );

    sapo_broker::Atom_Publish publish = action.publish();
    if( publish.destination_type() == sapo_broker::Atom_DestinationType_TOPIC )
        printf("TOPIC: OK\n");
    else
        printf("TOPIC: BAD\n");


    sapo_broker::Atom_BrokerMessage message = publish.message();


    *dest = publish.destination().c_str();
    *payload = message.payload().c_str();
    *len = strlen(*payload);

    return;
}

int main(void)
{
    std::string msg;
    const char *payload;
    const char *dst;
    unsigned len;

    create_publish( DEST, PAYLOAD, strlen(PAYLOAD), &msg);

    extract_publish( &msg, &payload, &len, &dst );

    if( !strcmp(DEST, dst) )
        printf("DESTINATION NAME: OK\n" );
    else
        printf("DESTINATION NAME: BAD (-%s- IS NOT -%s-)\n", dst, DEST);

    if( !strcmp(payload, PAYLOAD) )
        printf("PAYLOAD: OK\n" );
    else
        printf("PAYLOAD: BAD (-%s- IS NOT -%s-)\n", payload, PAYLOAD );
    return 0;
}


