Password for CLIENT Key-, Truststore: "client"
Password for BROKER Key-, Truststore: "broker"

Get it running:

1. upload client.crt in Communote administration section
2. add Broker certificate to Communote Keystore (COMMUNOTE_KEY_STORE_PASSWORD is the password of the keystore which is unique for every installation. Can be found in table configuration_app_setting) 

keytool 
	-importkeystore 
	-deststorepass COMMUNOTE_KEY_STORE_PASSWORD 
	-destkeypass COMMUNOTE_KEY_STORE_PASSWORD
	-destkeystore communote.ks 
	-srckeystore broker.ks 
	-srcstoretype jks 
	-srcstorepass broker 
	-alias communote-mq-broker

List keys:

keytool -list -keystore communote.ks -storepass COMMUNOTE_KEY_STORE_PASSWORD


