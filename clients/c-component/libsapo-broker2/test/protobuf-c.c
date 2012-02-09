#include "broker-a"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

int main()
{
  SapoBroker__Atom atom = SAPO_BROKER__ATOM__INIT;
  SapoBroker__Atom__Action action = SAPO_BROKER__ATOM__ACTION__INIT;
  SapoBroker__Atom__Publish publish = SAPO_BROKER__ATOM__PUBLISH__INIT;
  SapoBroker__Atom__BrokerMessage message = SAPO_BROKER__ATOM__BROKER_MESSAGE__INIT;


  publish.destination = "/protobuf-c/topic";
  publish.destination_type = SAPO_BROKER__ATOM__ACTION__ACTION_TYPE__PUBLISH;

  message


  SapoBroker__Atom *atom2;
  unsigned char simple_pad[8];
  size_t size, size2;
  unsigned char *packed;
  ProtobufCBufferSimple bs = PROTOBUF_C_BUFFER_SIMPLE_INIT (simple_pad);

  atom.name = "dave b";
  atom.id = 42;
  size = foo__atom__get_packed_size (&atom);
  packed = malloc (size);
  assert (packed);
  size2 = foo__atom__pack (&atom, packed);
  assert (size == size2);
  foo__atom__pack_to_buffer (&atom, &bs.base);
  assert (bs.len == size);
  assert (memcmp (bs.data, packed, size) == 0);
  PROTOBUF_C_BUFFER_SIMPLE_CLEAR (&bs);
  atom2 = foo__atom__unpack (NULL, size, packed);
  assert (atom2 != NULL);
  assert (atom2->id == 42);
  assert (strcmp (atom2->name, "dave b") == 0);

  foo__atom__free_unpacked (atom2, NULL);
  free (packed);

  printf ("test succeeded.\n");

  return 0;
}
