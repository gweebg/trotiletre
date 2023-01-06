### Project Requirements

---

#### Funcionalidades Base

+ Assuma uma distribuição aleatória de uma dado número fixo de trotinetes pelo mapa,
todas livres, quando o servidor arranca.


+ O mapa é uma matriz N x N locais, sendo as coordenadas geográficas pares discretos de índices.
    + A distância entre dois pontos é medida pela distância de Manhattan.


+ O utilizador **regista-se** e **autentica-se** fornecendo um nome e palavra-passe.
   + O cliente deve estabelecer uma conexão com o serviço e ser autenticado.


+ Listar locais com trotinetes livres até uma distânica fixa D de um local.


+ Listar recompensas com origem até uma distância fixa D de um determinado local dada por pares origem destino.


+ Reservar uma trotinete livre o mais perto possível de determinado local limitado a uma distância fixa D.
   + O serviço deverá responder com o **local** e um **código de reserva** ou código de insucesso caso não seja possível. 

    
+ Estacionar uma trotinete fornecendo o código de reserva e o local.
    + O servidor informa ao cliente o custo da viagem, em função do tempo passado desde a reserva e a distância.
    + Se a viagem for uma recompensa, é informado o valor da recompensa.


+ Cliente pode ativar notificações para as recompensas a menos de uma distância fixa de D.
  + As notificações podem ser enviadas a qualquer momento.
  + O cliente pode desativar as notificações.

---

#### Requisitos das Recompensas

+ A geração de recompensas deve ser gerado em background.


+ Deve gerar uma recompensa para movimentar uma trotinete de um dado local A para um local B quando: há mais do que uma 
trotinete livre em A e não há nenhuma livre num raio D de B.


+ A geração de recompensas deve ser realizado sempre que e apenas quando alguma trotinete livre é reservada
ou estacionada.


+ A cada momento existe uma lista de recompensas atualizada ao longo do tempo.
    + Cada recompensa é identificada por um par (x,y).
    + Cada recompensa tem um valor monetário associado calculado em função da distância a percorrer.