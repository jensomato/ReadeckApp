#!/usr/bin/env bash
KEYSTORE="/home/jd/Nextcloud/intranet/Daten/10-19 Projects/18 ReadeckApp/18.10 Build/ReadeckApp-keystore.jks" \
	KEY_ALIAS=signing \
	KEYSTORE_PASSWORD=$(pass keystore/ReadeckApp) \
	KEY_PASSWORD=$(pass keystore/ReadeckApp) \
	./gradlew $@

