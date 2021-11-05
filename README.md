# Distribution Computation Problem (HW-3)
### Description: The goal of this homework is for students to gain experience with solving a distributed computational problem using cloud computing technologies by designing and implementing a RESTful service and a lambda function that are accessed from clients using gRPC.
( CS441 | Fall 2021 )
## Name: Samihan Nandedkar
### UIN: 667140409

Please find below the video explaining the execution and deployment for project:

### Running the project.

### Project Structure
This project can be divided into 4 major parts. 
1. Setup LogFileGenerator in a EC2 instance and update the log files in S3 bucket periodically.
2. Create a AWS Lambda function to perform the task of searching for logs message in given time frame. 
3. Invoking the Lambda Function through API Gateway using POST/GET method.
4. Create a client-server model to trigger the AWS Lambda function though gRPC framework.

## Part 1: LogFileGenerator in EC2 Instance. 

### Updating LogFileGenerator code
Updated Forked repository: https://github.com/samihann/LogFileGenerator

The LogFileGenerator should be periodically running in EC2 instance at designated time and keep on appending the logs
to messages a single log file. And the updated log file should be placed in S3 bucket from where the Lambda Function can access it. 

* To achieve this functionality there are few changes made to LogFileGenerator logback.xml file. Updated code can be accessed in the 
forked repository mentioned above. 


![img.png](docs/img.png)


* The files will be updated to a single file named `LogFileGenerator.log`, and each log message will have datetime stamp in the following format. 
```
2021-10-31 23:34:50.208 [main] INFO  GenerateLogData$ - Log data generator started...
```
### Create EC2 Instance & setup environment
* Generated a EC2 instance with Amazon Linux image and installed all the required packages in the instance. 

![img_1.png](docs/img_1.png)

* Set up the LogFileGenerator project in the instance to be run using sbt.
* Create and attach a IAM role to EC2 instance to provide S3 access. 
### Cron
* To run the project periodically in the linux instance, Cron is utilized. 
* All the terminal commands required to run the application are put in a shell script.
* At the designated time, the cron will execute the shell script and run the application. 

![img_2.png](docs/img_2.png)

* Please refer file to see the shell script. 
* AWS CLI is used to copy the updated log file to S3 bucket where the lambda function can access it. 
* Please refer below to see the crontab entry.

![img_3.png](docs/img_3.png)

* At 23:55 each day, the uploadscript.sh will be executed which will add logs messages to LogGenerator.log file.

## Part 2: AWS Lambda

### Code

* The program is written in Python. 
* It is taking time, deltaTime & pattern as input and returns if the pattern is present in the time frame.
* The response will be returned with appropriate status code. 

The code is binary search to through the log file making the time complexity of code as O(log(n))

## API Gateway

* To trigger the Lambda function, it is connected with API Gateway with GET and POST method defined. 

![img_4.png](docs/img_4.png)

* Whenever a GET/POST request is made to the created API gateway, Lambda function will be triggered taking the query parameters/payload as input.

## Part 3: POST/GET Request Client. 

Package: _com.samihann.rest_

Packages used to make calls: Apache HTTP, Scala.io.Source

* `SearchRestClient` contains the client function to make POST and GET request to lambda function.

### Simulation:

* To run the application, please navigate to project root directory and run the following command to run the project. 
```
sbt clean compile run
```
* Please run `SearchRestClient`, which will provide user with option to make POST/GET request. 
![img_5.png](docs/img_5.png)
* Please select the required option to start the simulation adn see the output. 

### Output



## Part 4: gRPC Client/Server


### Compile and Create Jar
The project can be compiled to create jar using the following procedure.
* Through Command Line
  * Clone the following repository in your system.
  * On command line please navigate to the following repository. And run the following commands to compile and run the code. 
  ```
  sbt clean compile test
    ``` 
    ```
  sbt assembly
    ```
A .jar file should be created in /target/scala-3.01/project name.jar
  
### Project Structure
#### Driver
`DriverClass` contains the configuration of all the MapReduce jobs to be executed. This class is called whenever the 
jar is executed in hadoop.

#### Mappers
Package: com.samihann.mappers

This package contains all the mappers required for all the jobs: 
`JobOneMapper`, `JobTwoMapper`, `JobTwoMapper`, `JobThreeMapper`.

#### Reducers
Package: com.samihann.reducers

This package contains all the reducers required for all the jobs:
`JobOneReducers`, `JobTwoReducers`, `JobTwoReducers`, `JobThreeReducers`

#### Utility
Package: com.samihann.Utility

#### Configuration

The configuration.conf file contains the parameters which are used in the code.

This package contains all the utility functions which are reused across the jobs such as parsing task. 

### Hadoop Map Reduce
![img.png](docs/img.png)
* Hadoop is a framework that allows us to store and process large data sets in parallel and distributed fashion.
* There are major two components of a Hadoop framework. 
  * Storage: HDFS - Distributed file system. (Hadoop Cluster)
  * MapReduce - Parallel and distributed processing.
* HDFS: It is the primary data storage system under Hadoop applications.
  * NameNode:
    * Receives heartbeat and block report from DataNode
    * Records metadata
  * DataNode: 
    * Stores actual data.
    * Handles servers read and write request.
* MapReduce:
  * Mappers: User designated code which gives a out of Key Value pair. 
  * Reducers: User designated code which takes input of key and list[Values] pair and give our a key value pair according to logic defined. 
  
## Jobs

In this project we have used Hadoop MapReduce to perform the below mentioned tasks. 

1. **Job 1:** 
* **Compute the distribution of different types of messages across predefined time intervals and injected string instances of the designated regex pattern for these log message types.**

   * Mapper: `JobOneMapper`: [Open](/src/main/scala/com/samihann/mappers/JobOneMapper.scala)
   * Reducer: `JobOneReducer`: [Open](/src/main/scala/com/samihann/reducers/JobOneReducer.scala)
   
Logic:

The Mapper will do the task of parsing through the file to do the following three checks
* Check if the message lies in the specifies time interval.
* Check the message type and designate the type as _key_
* Check if the message contains the given regex patter, if it does give One(IntWritable) in _value_

The reducer will go over all the values for a message type and add the using foldLeft function to iterate through it.

Output:

In the configuration.conf, the start time and end time is mentioned as below.
![img_1.png](docs/img_1.png)

The mapreduce job is showing the output for the message grouped by their type in this time frame.

![img_3.png](docs/img_3.png)

2. **Job 2:** 
* **Compute time intervals sorted in the descending order that contained most log messages of the type ERROR with injected regex pattern string instances.**
* Mapper: `JobTwoMapper`: [Open](/src/main/scala/com/samihann/mappers/JobTwoMapper.scala)
* Reducer: `JobTwoReducer`: [Open](/src/main/scala/com/samihann/reducers/JobTwoReducer.scala)

Logic: 

The Mapper will do the task of parsing through the file to do the following task
* On the first iteration it will set start-time as log time and end-time as start-time + 5 mins.
* Designate the time interval as _key_
* Check if the message type is ERROR and contains the given regex patter, if it does give One(IntWritable) in _value_

The reducer will go over all the values for a interval and add the using foldLeft function to iterate through it.

Output:

In the configuration.conf, the duration is mentioned as below.
![img_2.png](docs/img_2.png)

The mapreduce job will show the output for the message grouped by their time interval.

![img_5.png](docs/img_5.png)


4. **Job 3:** 
* **Produce the number of the generated log messages for each message type.**
* Mapper: `JobThreeMapper`: [Open](/src/main/scala/com/samihann/mappers/JobThreeMapper.scala)
* Reducer: `JobThreeReducer`: [Open](/src/main/scala/com/samihann/reducers/JobThreeReducer.scala)

Logic:

The Mapper will do the task of parsing through the file to do the following task
* In the log message check if the message type.
* Designate the type as _key_
* Check all the messages of a particular type contains the given regex patter, if it does give One(IntWritable) in _value_

The reducer will go over all the values for a interval and add the using foldLeft function to iterate through it.

Output:

The mapreduce job will show the output for the message grouped by their type.

![img_6.png](docs/img_6.png)

5. Job 4: 
* **Produce the highest number of characters in log message for each log message type that contain the detected instances of the designated regex pattern.**
* Mapper: `JobFourMapper`: [Open](/src/main/scala/com/samihann/mappers/JobFourMapper.scala)
* Reducer: `JobFourReducer`: [Open](/src/main/scala/com/samihann/reducers/JobFourReducer.scala)

Logic:

The Mapper will do the task of parsing through the file to do the following task
* In the log message check if the message type.
* Designate the type as _key_
* Check all the messages of a particular type contains the given regex patter, if it does count the characters in message and send the count as _value_

The reducer will go over all the values for a interval and find the maximum character count using foldLeft function to iterate through it.

Output:

The mapreduce job will show the output of hight character count for the messages grouped by their type.

![img_7.png](docs/img_7.png)


### Test Cases

* Through Command Line
  * Clone the following repository in your system.
  * On command line please navigate to the following repository. And run the following commands to compile and run the test cases.
  
  ```
  sbt clean compile test
    ``` 

![img_8.png](docs/img_8.png)


### References

* https://github.com/0x1DOCD00D/LogFileGenerator
* https://www.cloudera.com/
* https://aws.amazon.com/education/awseducate/
* https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html







