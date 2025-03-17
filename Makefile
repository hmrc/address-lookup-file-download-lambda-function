TARGET_PATH := target/scala-3.3.5
ARTIFACT := address-lookup-file-download-lambda-functions_3-1.2.2.0

test:
	sbt test

build:
	sbt clean test assembly
	cd $(TARGET_PATH); openssl dgst -sha256 -binary $(ARTIFACT).jar | openssl enc -base64 > $(ARTIFACT).base64sha256

push-s3:
	aws s3 cp $(TARGET_PATH)/$(ARTIFACT).jar s3://$(S3_BUCKET)/$(ARTIFACT).jar --acl=bucket-owner-full-control
	aws s3 cp $(TARGET_PATH)/$(ARTIFACT).base64sha256 s3://$(S3_BUCKET)/$(ARTIFACT).base64sha256 --acl=bucket-owner-full-control --content-type=text/plain
	aws s3 cp $(TARGET_PATH)/$(ARTIFACT).base64sha256 s3://$(S3_BUCKET)/$(ARTIFACT).jar.base64sha256 --acl=bucket-owner-full-control --content-type=text/plain
