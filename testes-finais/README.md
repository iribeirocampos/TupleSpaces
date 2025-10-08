# Bateria de testes para a solução final do projeto TupleSpaces - SD 2025

Este conjunto de testes exercita diferentes situações de concorrência entre clientes após implementação de optimizações de desempenho do Algoritmo Xu Liskov e a implementação do take com regex.

Estes testes não são avaliados automaticamente. Quem executa cada teste deve observar o comportamento de cada cliente concorrente (incluindo os instantes em que cada *output* é impresso por cada cliente) e, assim, perceber se o comportamento observado é o esperado.

## Antes de começar

- Devem ser iniciados 3 ReplicatorServer com as portas 3001, 3002 e 3003
- Deve ser iniciado um FrontEnd com os argumentos "localhost:3000 3001 3002 3003"
- Devem ser iniciados 2 clientes com os ids 1 e 4 ou outra combinação que que resulte no mesmo voter_set para maximizar as colisões e respetivos problemas.

## Resumo dos testes:

- test1: Testa cenário em que 2 clientes com o mesmo voter_set tentam fazer o take do mesmo tuplo, em que 1 cliente consegue fazer lock de um servidor e o C2 consegue fazer o lock do outro servidor gerando uma possivel situação de deadlock. Ambos os clientes devem fazer backoff e na segunda tentativa um dos cleintes deve conseguir adquiriri o lock e fazer o respeito take e o outro cliente deve ficar a aguardar do put seguinte para conseguir fazer lock e respetivo take.

- test2: Testa `read` após `take` invocados consecutivamente pelo mesmo cliente, e o front-end espera que a execução do take para executar o read e assim não devolver valores incoerentes ao cliente

- test3: Testa invocações concorrentes de `take` que competem pelo mesmo espaço de tuplos. Ou sejam a nossa implementação irá permitir takes concorrentes sem entrar em deadlock ou ter comportamentos incoerentes. Cada cliente tentará fazer take da mesma expressão regular e ambos conseguiram executar.« concorrentemente.


## Instruções:
- Deve ter os servidores e o front-end lançados previamente. 
- Para executar cada teste, lance ambos os clientes simultaneamente, cada um numa janela diferente do terminal. Assim poderá observar ambos, lado a lado. 
