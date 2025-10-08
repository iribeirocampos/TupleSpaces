# TupleSpaces

This document describes the 2024/2025 Distributed Systems course project.

*Preliminary note:* We decided to publish the project statement on the first day of classes to allow students to start reading it and anticipate what will happen during the term.  
However, the actual project work should only start in the 2nd week of classes, which coincides with the lab sessions on *gRPC* programming. Therefore, the initial project code will only be made available at that time.

## 1 Introduction

The goal of the Distributed Systems (DS) project is to develop the **TupleSpaces** system, a service that implements a distributed *tuple space*.  
The system will be implemented using [gRPC](https://grpc.io/) and Java (with one exception, described later in this statement).

The service allows one or more users (also called _workers_ in the literature) to place tuples in the shared space, read existing tuples, and remove tuples from the space. A tuple is an ordered set of fields *<field_1, field_2, ..., field_n>*.  
In this project, a tuple should be instantiated as a character string (*string*).  
For example, the *string* containing `"<vaga,sd,turno1>"`.

Multiple identical instances can coexist in the tuple space.  
For example, multiple tuples `"<vaga,sd,turno1>"` may exist, indicating several available slots.

It is possible to search in the tuple space for a given tuple to read or remove.  
In the simplest variant, one can search for a concrete tuple. For example, `"<vaga,sd,turno1>"`.  
Alternatively, it is possible to use [Java regular expressions](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) to match multiple values. For example, `"<vaga,sd,[^,]+>"` matches both `"<vaga,sd,turno1>"` and `"<vaga,sd,turno2>"`.

More information on distributed tuple spaces, as well as a description of a system implementing this abstraction, can be found in the course bibliography and in the following article:

A. Xu and B. Liskov. [A design for a fault-tolerant, distributed implementation of linda](http://www.ai.mit.edu/projects/aries/papers/programming/linda.pdf). In 1989 The Nineteenth International Symposium on Fault-Tolerant Computing. Digest of Papers(FTCS), pages 199–206.

The operations available to the user are as follows [^1]: *put*, *read*, *take*, and *getTupleSpacesState*.

[^1]: We use the English terminology from the course bibliography, but we replaced the name *write* with *put*, which seems clearer. Note that the original article uses different terminology.

* The *put* operation adds a tuple to the shared space.

* The *read* operation accepts a tuple description (possibly with a regular expression) and returns *one* tuple that matches the description, if one exists. This operation blocks the client until a matching tuple exists. The tuple is *not* removed from the tuple space.

* The *take* operation accepts a tuple description (possibly with a regular expression) and returns *one* tuple that matches the description. This operation blocks the client until a matching tuple exists. The tuple *is* removed from the tuple space.

* The *getTupleSpacesState* operation takes no arguments and returns a list of all tuples on each server.

Users access the **TupleSpaces** service through a client process, which interacts with one or more servers offering the service via remote procedure calls.

## 2 Project Objectives and Stages

In this project, students will:

- Develop a distributed system using a modern RPC framework (gRPC), practicing its main communication models (*blocking and non-blocking stubs*).

- Replicate a distributed service using a realistic architecture.

- Understand how concurrency is prevalent in a distributed system: not only concurrency between distributed processes, but also concurrency between *threads* running on servers.  
Based on this concurrency, implement algorithms that ensure the desired consistency.

- Get in touch with academic research papers describing some of the algorithms implemented in the project.  
Understand how advances in these scientific domains are described in such papers.

The project has three objectives. Two are mandatory and one is optional.

For each objective, multiple stages are defined. Below we describe each objective and its stages.

### Objective A

Develop a solution in which the service is provided by a single server (i.e., a simple client-server architecture without server replication), which accepts requests at a well-known address/port.  
Clients interact with a replication *front-end*, which acts as a mediator with the server.  
Both clients and the *front-end* must use gRPC *blocking stubs*.

#### Stage A.1

System implemented without a *front-end*, where clients interact directly with the server.  
Two clients are available, one implemented in Java and the other in Python.

#### Stage A.2

With a *front-end* between clients and the server.

Note: The system must support multiple *front-ends* running simultaneously, each serving a subset of clients.  
However, for this project, only the single *front-end* case will be tested.

### Objective B

Develop an alternative solution where the service is replicated across **three servers**.  
In this solution, the *front-end* will need to use gRPC *non-blocking stubs*.

The remote interface (a `.proto` file) for the replicated servers is not provided in the base code.  
Each group must create this remote interface. We recommend adapting the `TupleSpaces.proto` provided by the instructors. Interfaces that diverge unnecessarily from `TupleSpaces.proto` will be penalized.

#### Stage B.1

Develop the _read_ and _put_ operations (without supporting _take_ for now)  
following the Xu & Liskov algorithm (cited above).  
Briefly, when a client invokes one of these operations, the *front-end* first sends the request to all servers and then waits for responses (from one server in the case of _read_, or from all servers in the case of _put_).

To allow debugging of the replicated system, clients may optionally specify a delay (in seconds) that each replica should wait before executing a replicated operation (*read*, *put*, and later *take*).  
The delay for each request should be sent as *gRPC metadata* in the request to the *front-end* and forwarded to the replicas.

#### Stage B.2

Also develop the code to execute the _take_ operation.  
Instead of the solution proposed in the Xu/Liskov algorithm, a solution based on **Maekawa’s mutual exclusion algorithm** (described in the course bibliography) should be developed.

Conceptually, the *front-end* should implement a _take_ request in three steps:

1. Enter the critical section (according to Maekawa’s algorithm),
2. Once in mutual exclusion, invoke the _take_ operation on all replicas and wait for responses from all,
3. Exit the critical section (according to Maekawa’s algorithm).

The following constraints must be observed:

- Regarding the centralized tuple space built in the previous stage, the replicated solution must assume that the _take_ operation can only receive a concrete tuple (i.e., regular expressions are not allowed as an argument for the replicated _take_ operation).

- Each client is assumed to have a numeric *client_id*, passed as an argument when the client starts.  
Given this *client_id*, the *voter set, V_i,* used by Maekawa’s algorithm must be: *{client_id mod 3, (client_id + 1) mod 3}* (each element identifies a replica, from 0 to 2).

- Handling deadlocks is outside the scope of this project.

- The algorithm described in Maekawa’s original paper includes important differences that should **not** be considered for this project.  
In other words, the reference is the algorithm described in the course bibliography.

Implementations that, while respecting the design above, allow the replicated system to serve *take* requests for different tuples in parallel will be valued.

### Objective C

Refine the solution obtained in the previous objective.

#### Stage C.1

Extend the solution so that the *take* operation can also accept a regular expression as an argument.  
As in the previously built solution, the first step of the algorithm continues to send the request to a *voter set* only.

#### Stage C.2

Optimize the solution composed in Stage B.2, attempting to reduce the number of exchanged messages and/or the waiting time in the *front-end* critical path.

Suggestion: see the discussion in section 4.2 of Xu and Liskov.

For submission of the solution for stages C.1 and/or C.2, in addition to the code of the solution, each group must also submit a document of **maximum 2 pages** describing the solution design. The format of that document is available [here](https://github.com/tecnico-distsys/Tuplespaces-2025/blob/master/OrientacoesRelatorioFinalSD.md).


## 4 Processes

### *TupleSpaces* Servers

Servers must be launched by receiving their port as the single argument.  
For example (**$** represents the system shell):

`$ mvn exec:java -Dexec.args="3001"`

The remote interfaces to be used for the different implementations of the TupleSpaces server  
are defined in the *proto* files provided by the teaching staff along with this statement.

### Front-end

The *front-end* is simultaneously a server (since it receives and responds to client requests) and a client  
(since it makes remote invocations to the TupleSpaces server(s)).  
When launched, it receives the port on which it should provide its remote service, as well as the machine name  
and port pairs of the TupleSpaces servers it will interact with (one server in variant A, three servers in later variants).

For example, in stage 1.2 (still without replication), the *front-end* can be launched like this to use port 2001  
and connect to the TupleSpaces server on localhost:3001:

`$ mvn exec:java -Dexec.args="2001 localhost:3001"`

### Clients

Client processes receive commands from the console. All client processes must display the symbol *>* whenever they are waiting for a command to be entered.

For all commands, if no error occurs, client processes must print "OK" followed by the response message, as generated by the `toString()` method of the class generated by the `protoc` compiler, as illustrated in the examples below.

If a command causes an error on the server side, that error must be transmitted to the client using gRPC’s error handling mechanisms (in Java, encapsulated in exceptions).  
In such cases, when the client receives an exception after a remote invocation, it should simply print a message describing the corresponding error.

Both types of client programs receive as arguments the machine name and port where the TupleSpace *front-end* (or, in stage 1.1, the TupleSpaces server) can be found, as well as the *client-id* (see stage B.2).  
For example, the Java client can be launched like this:

`$ mvn exec:java -Dexec.args="localhost:2001 1"`

and the Python client can be launched like this:

`$ python3 client_main.py localhost:2001 1`

For stage 2.2 (the _take_ operation), client programs must receive a client identifier argument  
(an integer assumed to be unique among client processes).

There is one command for each service operation: `put`, `read`, `take`, and `getTupleSpacesState`.  
The first three receive a *string* delimited by `<` and `>` with no spaces between those symbols, defining a tuple or, in the case of `read` and `take`, a regular expression (using Java’s regular expression syntax) specifying the desired tuple pattern.

Example:

```
put <vaga,sd,turno1>
OK

put <vaga,sd,turno2>
OK

take <vaga,sd,turno1>
OK
<vaga,sd,turno1>

read <vaga,sd,[^,]+>
OK
<vaga,sd,turno2>
```

From stage B.1 onwards, any of the above commands may receive 3 additional *optional* integer arguments (non-negative).  
These integers define the delays that each replica must apply before executing the request (see description of stage B.1).

There are also two additional commands that do not result in remote invocations:

- `sleep`, which blocks the client for the number of seconds given as its single argument.
- `exit`, which terminates the client.


## 5 Other Considerations

### *Debug* Option

All processes must be executable with a "-debug" option. If this option is selected, the process should print to *stderr* messages describing the actions it performs.  
The format of these messages is free, but they should help debug the code and understand the execution flow during the final discussion.

### Interaction Model, Faults, and Security

You should assume that neither servers, nor *front-ends*, nor clients can fail.  
You should also assume that TCP connections (used by gRPC) handle message loss, reordering, or duplication.  
However, messages may be arbitrarily delayed, so the system is asynchronous.

Security-related problems (e.g., user authentication, message confidentiality or integrity) are out of scope for this project.

### Persistence

Persistent storage of server state is neither required nor graded.


### Code Quality

Code quality evaluation includes the following aspects:

- Correct configuration (POMs);
- Readable code (including relevant comments);
- [Proper exception handling](https://tecnico-distsys.github.io/04-grpc-erros.html);
- [Correct synchronization](https://tecnico-distsys.github.io/02-java-avancado.html);
- Separation of protoc/gRPC-generated classes from domain classes maintained in the server.