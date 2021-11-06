#!/bin/bash
cd /root/LogFileGenerator &&
sbt clean compile run &&
aws s3 cp /root/LogFileGenerator/log/LogFileGenerator.log s3://samihan-s3-bucket