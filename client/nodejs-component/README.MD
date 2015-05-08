nodejs-component

SAPO Broker NodeJS-Component

Do 'npm install' before you try to run the client. For an example, see 'client.js'.

WARNING: Node module node-expat dependes on the system package libexpat1-dev (Debian based OS).


Functions:

	SapoBroker.init( configs );

		configs
			Type: Object
			Values:
				port
					Type: String
					Default: 6623
					Example: 6623
				host
					Type: String
					Default: '10.135.66.175'
					Example: '10.135.66.175'
				transport
					Type: String
					Default: 'json'
					Options:
						'xml'
						'json'
				return
					Type: String
					Default: 'js'
					Options:
						'xml'
						'json'
						'js'
				callbacks
					Type: Object
					Values:
						onConnect
							Type: Function
						onData
							Type: Function
						onEnd
							Type: Function
						onTimeout
							Type: Function
						onDrain
							Type: Function
						onError
							Type: Function
						onClose
							Type: Function

	SapoBroker.configure( configs );
		
		configs
			Type: Object
			Values:
				port
					Type: String
					Default: 6623
					Example: 6623
				host
					Type: String
					Default: '10.135.66.175'
					Example: '10.135.66.175'
				transport
					Type: String
					Default: 'json'
					Options:
						'xml'
						'json'
				return
					Type: String
					Default: 'js'
					Options:
						'xml'
						'json'
						'js'
				callbacks
					Type: Object
					Values:
						onConnect
							Type: Function
						onData
							Type: Function
						onEnd
							Type: Function
						onTimeout
							Type: Function
						onDrain
							Type: Function
						onError
							Type: Function
						onClose
							Type: Function

	SapoBroker.connect();

	SapoBroker.disconnect();

	SapoBroker.reconnect();

	SapoBroker.subscribe( message, ackMode );

		message
			Type: Object
			Values:
				action_id
				(JSON TRANSPORT ONLY)
					Type: String
					OPTIONAL
				destinationName
					Type: String
				destinationType
					Type: String
					Options:
						'TOPIC'
						'QUEUE'
						'VIRTUAL_QUEUE'
		ackMode
			Type: String
			Default: 'AUTO'
			Options:
				'AUTO'

	SapoBroker.unsubscribe( message );

		message
			Type: Object
			Values:
				action_id
				(JSON TRANSPORT ONLY)
					Type: String
					OPTIONAL
				destinationName
					Type: String
				destinationType
					Type: String
					Options:
						'TOPIC'
						'QUEUE'
						'VIRTUAL_QUEUE'

	SapoBroker.acknowledge( message );

		message
			Type: Object
			Values:
				action_id
				(JSON TRANSPORT ONLY)
					Type: String
					OPTIONAL
				destinationName
					Type: String
				messageId
					Type: String

	SapoBroker.publish( message );

		message
			Type: Object
			Values:
				action_id
				(JSON TRANSPORT ONLY)
					Type: String
					OPTIONAL
				destinationName
					Type: String
				destinationType
				(JSON TRANSPORT ONLY)
					Type: String
					Options:
						'TOPIC'
						'QUEUE'
						'VIRTUAL_QUEUE'
				payload:
					Type: String
				messageId
					Type: String
					OPTIONAL
				expiration
					Type: Number
					OPTIONAL
				timestamp
					Type: Number
					OPTIONAL
				priority
				(XML TRANSPORT ONLY)
					Type: String
					OPTIONAL
				correlationId
				(XML TRANSPORT ONLY)
					Type: String
					OPTIONAL

	SapoBroker.enqueue( message );
	(XML TRANSPORT ONLY)

		message
			Type: Object
			Values:
				destinationName
					Type: String
				payload:
					Type: String
				messageId
					Type: String
					OPTIONAL
				expiration
					Type: Number
					OPTIONAL
				timestamp
					Type: Number
					OPTIONAL
				priority
					Type: String
					OPTIONAL
				correlationId
					Type: String
					OPTIONAL

	
	SapoBroker.poll( message );

		message
			Type: Object
			Values:
				action_id
				(JSON TRANSPORT ONLY)
					Type: String
					OPTIONAL
				destinationName
					Type: String
				timeout
				(JSON TRANSPORT ONLY)
					Type: Number