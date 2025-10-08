# <p align="center">School Project</p>

## School and Course
<img src="https://epg.ulisboa.pt/sites/ulisboa.pt/files/styles/logos_80px_vert/public/uo/logos/logo_ist.jpg?itok=2NCqbcIP" width="100" height="50">

[Instituto Superior Técnico](https://tecnico.ulisboa.pt/)

[Engenharia Informática e de Computadores](https://tecnico.ulisboa.pt/en/education/courses/undergraduate-programmes/computer-science-and-engineering/)

## Class Subject and Goals
### Class: [DS - Distributed Systems](https://fenix.tecnico.ulisboa.pt/cursos/leic-t/disciplina-curricular/1408903891910867)
### Goals

1. Distributed Systems Concepts

- Understand distributed shared memory abstractions (TupleSpaces/Linda).

- Learn the difference between centralized vs. replicated services.

- Recognize challenges like concurrency, consistency, and synchronization in distributed systems.

2. Remote Procedure Calls (RPC) and gRPC

- Gain hands-on experience with gRPC in Java (and Python client optionally).

- Learn to implement blocking and non-blocking stubs.

- Understand client-server and front-end-server interaction patterns in RPC systems.

3. Concurrency and Synchronization

- Handle multi-threading within servers and front-ends.

- Manage mutual exclusion in distributed systems using Maekawa’s algorithm.

- Understand the importance of critical sections and race conditions in distributed operations like take.

4. Fault-Tolerance and Replication

- Learn techniques for replicating state across multiple servers.

- Implement algorithms for consistent reads and writes (Xu & Liskov algorithm).

- Understand how replication affects latency, consistency, and correctness.
 
### Grade: 19/20 ![Grade](https://img.shields.io/badge/Grade-A-brightgreen)


## Problem Specification

Please read [Problem Specification](project_specs.md)

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.


<h2>Credits</h2>

- Author: <a href="https://github.com/iribeirocampos" target="_blank">Iuri Campos</a>

<h2>Copyright</h2>
This project is licensed under the terms of the MIT license and protected by IST Honor Code and Community Code of Conduct. <br>
**Disclaimer:**  
A portion of the source code was provided as starter material by the course instructors.  

