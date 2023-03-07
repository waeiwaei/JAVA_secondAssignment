## Briefing on DB assignment
### Task 1: Introduction


This workbook will lead you through the next assessed exercise,
the aim of which is to build a database server (from the ground up !)
This will not only give you more practice writing complex Java programmes,
but will also provide you with hands-on experience of using query languages.
Note that assignment WILL contribute to your unit mark with a weighting of 35%.

The assignment will be marked on the lab machines, so it is essential that you check that you
can compile and run your code using Maven on these machines before submission.

You are encouraged to discuss assignments and possible solutions with other students.
HOWEVER it is essential that you only submit your own work.

This may feel like a grey area, however if you adhere to the following advice, you should be fine:

- Never exchange code with other students (via IM/email, GIT, forums, printouts, photos or any other means)
- Although pair programming is encouraged in some circumstances, on this unit you must type your own work !
- It's OK to seek help from online sources (e.g. Stack Overflow) but don't just cut-and-paste chunks of code...
- If you don't understand what a line of code actually does, you shouldn't be submitting it !
- Don't submit anything you couldn't re-implement under exam conditions (with a good textbook !)

If you ask a question on a discussion forum, try to keep discussion at a high level
(i.e. not pasting in chunks of your code). If it is unavoidable to include code, only share
small snippets of the essential sections you need assistance with.

An automated checker will be used to flag any incidences of possible plagiarism.
If the markers feel that intentional plagiarism has actually taken place, marks may be deducted.
In serious or extensive cases, the incident may be reported to the faculty plagiarism panel.
This may result in a mark of zero for the assignment, or perhaps even the entire unit
(if it is a repeat offence).
Don't panic - if you stick to the above list of advice, you should remain safe !  


#
### Task 2: Assignment Overview


In this assignment, you will build a relational database server from scratch.
This server should receive incoming requests (conforming to a standard query language)
and then interrogate and manipulate a set of stored records.
Your server will maintain persistent data as a number of files on your filesystem.
You will not be required to implement a client application - this will be provided for you
(to allow you to connect to your server and check that is it working correctly).

As usual, you have been provided with a <a href="resources/cw-db" target="_blank">Maven project</a>
to help get you started with the assignment.
As before, there is a <a href="resources/cw-db/src/test/java/edu/uob/ExampleDBTests.java" target="_blank">template test script</a>
for you to use - make sure you add suitable test cases to this script to ensure that your application is
fully and systematically tested. As with the previous assignment, you will be assessed on the extent to which
your test cases cover the implemented features of the software.

It is **essential** that your server is _robust_ - you should detect and trap errors effectively
and ensure that the server continues running at all times. Just imagine a world in which servers
had to be manually restarted every time something unexpected was encountered.
It's going to be very difficult for your server to pass the marking tests if it has crashed !

Note that your main class MUST be called `DBServer` and MUST include the following constructor method and input handling method:

* `public DBServer()`
* `public String handleCommand(String)`

If you change the name of the class or the signature of either of the above methods, we won't be able to run your code !
We will be using automated marking scripts and if you server does not conform to the above, we won't be able to test it !

Your submission will be assessed on the success with which it implements the described query language,
as well as the flexibility and robustness with which your server operates.
You will also be assessed on the "quality" of your source code (guidance will be provided during the assignment
to help you improve this aspect of your work).
Finally, since test-driven development is key to the processes taught on this programme,
the extent and quality of your test cases will also be considered.  


#
### Task 3: Persistent Storage
 <a href='03%20Persistent%20Storage/slides/segment-1.pdf' target='_blank'> ![](../../resources/icons/slides.png) </a> <a href='03%20Persistent%20Storage/video/segment-1.mp4' target='_blank'> ![](../../resources/icons/video.png) </a>

Any database system must be able to persistently store data (otherwise it will lose everything each time it is restarted).
In this assignment, you will use your file system for this purpose.
Your database will consist of a number of tables (aka **entities**), each containing a collection of rows
that store **records** - see the table shown later in this section for an example.

Each table should be stored in a separate file using tab separated text. A <a href="resources/people.tab" target="_blank">sample data file</a>
has been provided to illustrate this file format. Note that the constructor method of the `DBServer` class from the project template
initialises a `storageFolderPath` variable - this is the directory where your server should store its data files. It is essential that you store ALL of
your database files in this directory and nowhere else outside of it.

The first column in each table must contain a unique identifier or **primary key** (which should always be called `id`).
The `id` value of each row will NOT be provided by the user, but rather they should be automatically generated by the server.

Table names should be case insensitive (since some file systems have problems differentiating between upper and lower case).
Any table names provided by the user should be converted into lowercase before saving out to the filesystem.
You should treat column names as case insensitive for querying, but you should _preserve_ the case when storing them
(so that the user can make use of CamelCase if they wish).  


![](03%20Persistent%20Storage/images/table.jpg)

**Hints & Tips:**  
Your first task is to write a method that reads in the data from the <a href="resources/people.tab" target="_blank">sample data file</a>
using the Java File IO API. View the slides and video at the start of this section for an overview of these packages.
At this stage you need only print out the records to the screen (in a later section you will store this data in a suitable data structure).
You may need to delve more deeply into the
<a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/package-summary.html" target="_blank">File IO documentation</a>
in order to implement your ideas.

When working with file paths, it is **essential** that you do not use any platform-specific file separators.
Some platforms use `\` and some platforms use `/` for separating folder names in a file path.
Your java code should work on ALL platforms - for this reason, you should make use of `File.separator` constant
(which will contain the relevant character for the platform the code is running on).

Note that if you encounter a tab file with invalid formatting when reading in data from the filesystem, your file parsing method should
throw an `IOException`. You should ensure that this exception is subsequently caught by another part of your server - remember:
don't let your server crash !  


#
### Task 4: Maintaining Relationships


Relationships between records in different tables should be recorded using **foreign keys**.
For example, the `PurchaserID` in the table illustrated below is a reference to the `id` of a person from the table in the previous task.
This additional data file is <a href="resources/sheds.tab" target="_blank">provided for you</a> in order to aid you in developing a solution.
You may assume only single element keys are to be used (i.e. you do not need to cope with "composite" keys).

Note that the `id` of a record should NOT change at any time during the operation of the system
(once a record has been assigned an `id` this will stay fixed for as long as that record is kept in the database).
You should not "recycle" IDs in any table (e.g. if rows are deleted) since they might be used as foreign keys elsewhere !

For simplicity, no primary or foreign key marker keywords are provided by the query language - the server relies upon programmer remembering which attributes are keys.

It is _not_ your responsibility to normalise the database - this is a job for developers who have designed the database schema and who make use of your database server. If you don't know what normalisation is, then don't worry - you don't need to know for this assignment (although I bet you are intrigued to find out now ;o)



![](04%20Maintaining%20Relationships/images/sheds.jpg)

#
### Task 5: Java Data Structures


Once the data has been read in from the filesystem, you will need to store this in memory.
You will need to devise a suitable set of classes to represent this data inside your Java program.
Think carefully about the tabular nature of relational databases and then write a set of classes
that match this structure. You will need to consider a wide range of different elements of the
database, including: tables, rows, columns, keys, table names, column names, data values, ids
and relationships between entities.

Remember that this teaching block focuses on "development" (not just "coding") and as such we are
attempting to develop your analysis and design skills. This exercise is more than just implementing a
pre-defined specification - it requires you to understand the domain, be able to deconstruct
the problem and make informed design decisions to achieve a successful solution. As such,
it is not easy - you are likely to make mistakes and will need to refactor your code at different
stages over the next few weeks.

Once you have defined a collection of classes that you feel are appropriate to the problem,
use the file reading methods that you wrote in the previous section to populate instances of your classes
with data read in from the sample data files. In order to fully exercise your classes as you develop your server further,
you will need to write additional data files to augment those given to you. We advise creating these data files
using queries in your testing scripts, rather than manually writing them. This is because you are likely to need
to recreate them on a regular basis during the course of your development work.
Additionally, you should not write any test scripts that assume the presence of pre-existing tab files,
since there is no guarantee that these will be present in the databases folder !

Once you have successfully stored the imported data in your classes, the next step is to write a
method to save these structures _back out_ to the filesystem again
(using appropriate features of the Java File API).  


**Hints & Tips:**  
In order to check that your code is successfully reading and writing data from the files,
you should alter the data _whilst it is in memory_ (i.e. _after_ you have read it in but _before_ you write it back out again).
You could for example replace the age of all people in the table with a randomly generated one.
By changing the data in this way, you can check to make sure that the file system files are actually being over-written and updated !  


#
### Task 6: Communication


It is not the aim of this assignment to address the topic of network or socket programming.
For this reason, the networking aspects of the server have been provided for you
in <a href="resources/cw-db/src/main/java/edu/uob/DBServer.java" target="_blank">the template server class</a>.

The database server listens on port 8888 in order to receive incoming messages. These incoming commands are then
passed to the `handleCommand` method for processing. Your task is to add to the `handleCommand` method to respond to the commands.
At this stage in the assignment, you should not attempt to interpret the content of the incoming messages.
Rather it should respond with the content of _all_ of the tables currently in your database
(irrespective of what the incoming message contained).

You should attempt to make the response as human-reader friendly as possible. It is important that your test cases do not
check for exact formatting (since this may vary from implementation to implementation).
You will see from the sample test script provided in the template project how tests can be written using simple word matching.
You should use the same approach when writing your own tests.

It is essential that your response is returned by the `handleCommand` method and NOT just printed out in the local console.
When we test your server during the marking process, we will be monitoring what is returned via the network.
You won't get any marks for just doing `println` messages in the terminal !

To help you ensure that your server conforms to the correct protocol, a
<a href="resources/cw-db/src/main/java/edu/uob/DBClient.java" target="_blank">command-line client</a>
has been provided for you. This client can be run from the command line using `mvnw exec:java@client`
You should not have to change any of the code in the client, any features that you implement in this class
will not be executed during the marking process.
The client has been provided purely to allow you to manually check that your server is operating correctly.
During the marking process, this client will be discarded and replaced by an automated testing component.

For the sake of simplicity, you may assume only a single client is connected at any one time.
(i.e. there is no need to handle parallel queries or deal with issues of contention).  


#
### Task 7: Query Language


Now that you have a communication mechanism in place, we need something to transmit !
Clients will communicate with the server using a simplified version of the SQL database query language.
Your task is to write a handler for the incoming messages which will: parse the incoming command, perform the specified queries, update the data stored in the database and return an appropriate result to the client.

The query language we shall use for this purpose supports the following main types of query:

- USE: changes the database against which the following queries will be run
- CREATE: constructs a new database or table (depending on the provided parameters)
- INSERT: adds a new record (row) to an existing table
- SELECT: searches for records that match the given condition
- UPDATE: changes the existing data contained within a table
- ALTER: changes the structure (columns) of an existing table
- DELETE: removes records that match the given condition from an existing table
- DROP: removes a specified table from a database, or removes the entire database
- JOIN: performs an **inner** join on two tables (returning all permutations of all matching records)

A grammar that fully defines the simplified query language is provided in <a href="resources/BNF.txt" target="_blank">this BNF document</a>.
To help illustrate the use of the query language, we have also provided <a href="resources/example-transcript.docx" target="_blank">a transcript of example queries</a>.
You will note that BNF grammar contains two distinct types of rule:
- Symbols with angle brackets `<name>` denote rules which MAY contain arbitrary additional whitespace
- Symbols with square brackets `[name]` indicate rules that can NOT contain additional whitespace

As a consequence of these rules, your server should be able to correctly parse incoming commands irrespective of the number of _additional_ whitespace
characters between certain tokens. So for example:
<pre>SELECT    *  FROM     people  WHERE   Name  ==  'Steve' ;</pre>
is valid and acceptable, being equivalent to:
<pre>SELECT * FROM people WHERE Name=='Steve';</pre>

Dealing the such additional whitespace might seem like a daunting and challenging task.
Don't panic, we will provide you some help and advice in the lectures to deal with such queries.  


**Hints & Tips:**  
Note that you should NOT use any existing parsers or parser generators (Yacc, Lex, Antlr etc.) The aim of this assignment is to implement the command parsing using your own code.  


#
### Task 8: Query Specifics


This section provides more detail regarding the implementation of some of particular aspects of the query language.

#### _SQL keywords_
Convention has it that query examples are typically shown with uppercase keywords (to differentiate them from identifiers and literals). However, SQL keywords are in fact case insensitive, so `select * from people;` is equivalent to `SELECT * FROM people;`. This is true for all keywords in the BNF (including `TRUE`/`FALSE`, `AND`/`OR`, `LIKE` etc.)
In addition to this, all SQL keywords are reserved words, therefore you should not allow them to be used as database/table/attribute names.

#### _Comparisons_
It is not necessary to implement a datatype system within the database - you can just store everything as simple text strings.
You should perform >, =>, <, =< comparisons wherever it is possible to do something sensible (e.g. floats, ints, strings etc).
In situations where no appropriate comparison is possible (e.g. `TRUE > FALSE`) just return no data in the results table.
The `LIKE` comparator is just for use with strings and provides a simple substring matcher (NOT the full SQL `LIKE` operator with % or wildcarding).

#### _Response tables_
The order of values returned by a `SELECT` should be the same as specified in the query (e.g. `SELECT name, id FROM marks` would return the name column first, followed by the id column). Note that `SELECT *` should return the values in the order that they are stored in the table. The table returned by a `JOIN` should contain attribute names in the form `OriginalTableName.AttributeName`
(see the <a href="resources/example-transcript.docx" target="_blank">query transcript</a> for specific examples).
This is so the user can determine which attributes came from which tables (as well as coping with the situation where
two tables have attributes of the same name). The joined table should NOT contain the ID columns from the original tables,
but rather should include a new ID column containing new sequentially generated IDs.



#
### Task 9: Error Handling


Your query interpreter should identify any errors in the structure and content of incoming queries.
The response returned from your server must begin with one of the following two "status" tags:

- `[OK]` for valid and successful queries, followed by the results of the query.
- `[ERROR]` if the query is invalid, followed by a suitable human-readable message that provides information about the nature of the error.

Errors should be returned to the client if the SQL is malformed (i.e. doesn't conform to the BNF) or when the user attempts to perform prohibited actions,
including (but not limited to):
- inserting too many (or too few) values into a table
- creating a database or table that already exists
- creating a table with duplicate column names (or trying to add a column with an existing name)
- removing the ID column from a table
- changing (updating) the ID of a record
- queries on non-existent databases, tables and columns
- queries which use invalid element names (e.g. reserved SQL keywords)

Errors should NOT be returned in situations where the user performs:
- a valid query that has no matches: just return the column names and no data rows
- a query to delete columns/tables/rows/database that contain data: the user should be free to perform destructive actions
- a comparison of two different types: attempt a sensible comparison if possible, return blank results if not

Note that because you are not required to maintain type information for the attributes
in a table, it will not be possible for you to validate the type of inserted data.
It is therefore the responsibility of the user to ensure that only numerical data is stored in
numerical attributes and string data is stored in string attributes etc.

You may wish to make use of exceptions to handle errors internally within your server, however these should NOT be returned to the client.
Java exceptions are for the benefit of Java programmers - they are not intended for use as user error messages.
It is essential that your response back to the client begin with the correct status tag (either `[OK]` or `[ERROR]`).
These will be used by the automated testing scripts during the marking process !  


#
### Task 10: Submission


You should create a zip archive of your entire Maven project and upload this via the "Assessment, submission and feedback"
section on this unit's Blackboard page. It is essential that you ensure your code compiles and runs before you submit it.

Your submission will be assessed on the success with which it implements the described query language,
as well as the flexibility and robustness with which it operates.

You should add a suitable range of tests to your project: your aim is to cover all required features
of the server and verify the correct operation of your code. The completeness and comprehensivity of your test cases will be taken
into account during the marking of this assignment. You submission will also be assessed on "code quality" as outlined in the lectures.

You should make sure your code can respond to _at the very least_ the "standard" query spacing (as illustrated in the
<a href="resources/example-transcript.docx" target="_blank">example transcript</a>).
As with any real implementation of SQL, you should also however try to support some level of variability and robustness.
Remember that different users may attempt to use different spacing standards - it would be nice to support them, not constrain them.

Make sure that your code does not contain anything specific to your computer (e.g. absolute file paths, operating system specific code etc).
Before submitting your code, we advise your to test your project on a computer _other than the one it was developed on_ (e.g. a lab machine).
Clear out all database files and then ensure the code compiles and runs correctly using Maven (using: `mvnw clean compile test`).
We will apply a penalty mark if we cannot run your code "out of the box" - we can't spend time fixing everyones projects before we mark them !

It is VERY important that you do NOT change the name or parameters of any of the classes and methods that you have been given.
Scripts will be used to automatically run your code to make sure it operates correctly -
if you change them, we won't be able to mark your code !
It is **ESSENTIAL** that you check your code still passes the original skeleton test script - if this does not pass these basic tests
then it is likely your code not not pass any of the other test scripts.

You should include your entire project structure when you submit your code, with all the files and folder
(including both your `main` source folder as well as the `test` folder containing your test scripts)
Remember that your main class MUST be called `DBServer` and should not change the signature of the constructor or the `handleCommand` method
(or we won't be able to run your code !)  


#
