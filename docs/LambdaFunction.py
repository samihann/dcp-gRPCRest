import json
import boto3
from datetime import datetime
from datetime import timedelta
import re
import hashlib

s3 = boto3.client("s3")

def lambda_handler(event, context):
    fileObj = s3.get_object(Bucket="samihan-s3-bucket", Key="LogFileGenerator.log")
    file_content = fileObj["Body"].read().decode("utf-8").split("\n")
    if event['httpMethod']=='POST':
        body = json.loads(event['body'])
        input_duration = body['time_duration']
        input = body['time']
        pattern = body['pattern']
    elif event['httpMethod']=='GET':
        input_duration = float(event['queryStringParameters']['time_duration'])
        inputtime = event['queryStringParameters']['time']
        inputdate = event['queryStringParameters']['date']
        input = inputdate+" "+inputtime
        pattern = event['queryStringParameters']['pattern']
    time = datetime.strptime(input, '%Y-%m-%d %H:%M:%S.%f')
    end_time = time + timedelta(minutes=input_duration)
    start_time = time - timedelta(minutes=input_duration)
    index = binary_search(file_content,time)
    print("index",index)
    if index == -1:
        return {
        'statusCode': 400,
        'body': "The time provided is not present"
        }
    else:
        print("in else")
        currTime = time
        currIndex = index
        patternList = []
        hashList = []
        while currTime > start_time:
            print(currTime,currIndex)
            patternresult = re.search(pattern, file_content[currIndex])
            a = hashlib.md5(file_content[currIndex].encode('utf-8')).hexdigest()
            if patternresult:
                patternList.append(patternresult.group())
                hashList.append(a)
            currIndex = currIndex-1
            if currIndex < 0:
                break
            t = re.search("(20[0-9]{2}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})", file_content[currIndex])
            if t:
                currTime = datetime.strptime(t.group(), '%Y-%m-%d %H:%M:%S.%f')
        currTime = time
        currIndex = index
        while currTime < end_time:
            print(currTime,currIndex)
            patternresult = re.search(pattern, file_content[currIndex])
            a = hashlib.md5(file_content[currIndex].encode('utf-8')).hexdigest()
            print(a)
            if patternresult:
                patternList.append(patternresult.group())
                hashList.append(a)
            currIndex = currIndex+1
            if currIndex > len(file_content)-1:
                break
            t = re.search("(20[0-9]{2}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})", file_content[currIndex])
            if t:
                currTime = datetime.strptime(t.group(), '%Y-%m-%d %H:%M:%S.%f')
        if len(patternList) == 0:
            return {
            'statusCode': 400,
            'body': "Messages with the pattern are not present in time frame."
            }
        else:
            a = "Patterns Present are {} and MD5 for each of the messages is {}".format(patternList,hashList)
            return {
            'statusCode': 200,
            'body': a
            }



def binary_search(logs,time):
    low = 0 # Low pointer for binary search
    high = len(logs) - 1 # High pointer for binary search
    mid = 0 # Initializing mid as zero

    while low <= high:
        mid = (high + low) // 2
        log_event = logs[mid]
        result = re.search("(20[0-9]{2}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})", log_event)
        if result:
            log_eventtime = datetime.strptime(result.group(), '%Y-%m-%d %H:%M:%S.%f')

        # If log event time is more than the final time, move high to just before mid to ignore the right half of log
        if log_eventtime > time:
            high = mid - 1


        # If log event time is less than the initial time, move low to just after mid to ignore the left half of log
        elif log_eventtime < time:
            low = mid + 1

        # means x is present at mid
        else:
            return (mid)
    return (-1)
