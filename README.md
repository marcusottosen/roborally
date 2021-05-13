# Roborally

## Hvordan spillet startes:
1. Unpack zip filen.
2. Lav en ny/ren database i MYSQL.
3. Åben projektet i din IDE.
4. Nagiver til Connetor.java og indtast de rette oplysninger til din databse.
5. Kør programmet i StartRoboRally.java.
6. Det foreslås at der bruges "board1".


## Spillets features:
Spillet har i denne version:
* 7 kort (move1, move2, move3, right, left, u-turn og lef/right).
* En dropdown menu som indeholder: Stop, Save Game og Exit Game.
* Mulighed for at gemme og loade spillet.
* 3 knapper som kan bruges til at: færdiggøre programmeringsfasen, execute alle programkort og execute hvert programkort for sig.
* Mulighed for at skubbe til andre spillere.
* En ny VBox over spillepladen som viser spillernes informationer, herunder navn, score og liv.
* Opnås der 3 checkpoints vinder spilleren og der vises en "vinderbox".
* Mister en spiller alle sine liv, kan spilleren ikke rykke sig.

### Felter:
* Checkpoint felt som giver en spiller et point når spilleren står på det. Tæller automatisk op.
* Et felt med en væg, som kan sættes til hhv. NORTH, SOUTH, EAST, WEST.
* Pits som fjerner spillerens liv.
* Conveyorbelts i både gul og blå som rykker spilleren.
* Gears i begge retninger som drejer spilleren.
* Toolbox som kan give spilleren nyt liv.
* PushPanel som skubber spilleren.
* En ikke-fungerende laser.


## Hvordan spillet spilles:
1. I denne version spilles spillet ved at:
2. Spillet køres fra IDE'en hvor du først vil blive bedt om at loade, starte et nyt spil eller at exit.
3. Ved tryk på "new game" vil du blive bedt om at angive antal spillere samt hvilket board der skal bruges.
4. Dernæst skal hver spiller trække op til 5 kommandokort op i programfelterne.
5. Der skal så trykkes på finish programming som låser kortene.
6. Spillet kan nu fortsætte ved tryk på "Execute Program" som udfører alle programkort indtil der evt. bedes om interaktion fra brugeren.
7. Der kan i stedet for trykkes på "Execute Current Register" som udfører et programkort af gangen.
8. Når alle programkort er blevet udført, gives der nye kommandokort og der skal nu programmeres igen.
9. Spillet slutter når en spiller har alle checkpoints.

