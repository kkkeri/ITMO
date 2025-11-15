male(юрий_кудияш).      female(александра_мелько).
male(михаил_симонов).   female(алла_симонова).
male(кирилл_панко).     female(лидия_норрис).
male(антон_кудияш).     female(наталья_симонова).
male(борис_кудияш).     female(светлана_кудияш).
male(митя_кудияш). female(алена_кудияш).
male(алексей_бутанин). female(кира_ершова).
male(виктор_бутанин).   female(светлана_бутанина).
male(михаил_кудияш). female(елена_кудияш).
male(я).           female(мария_кудияш).
male(александр_кудияш).
male(алексей_васько).  female(надежда_алексеева).
male(андрей_васько).   female(надежда_васько).
male(дмитрий_ляхов).   female(любовь_громова).
female(галина_васько). female(наталья_васько).
male(сергей_фоменко).


/* === РОЖДЕНИЕ/СМЕРТЬ === */
born(юрий_кудияш,1890).   died(юрий_кудияш,1967).
born(александра_мелько,1892). died(александра_мелько,1969).
born(михаил_симонов,1889). died(михаил_симонов,1966).
born(алла_симонова,1889).  died(алла_симонова,1966).
born(кирилл_панко,1917).   died(кирилл_панко,2000).
born(лидия_норрис,1918).   died(лидия_норрис,1985).

born(антон_кудияш,1911).   died(антон_кудияш,1988).
born(наталья_симонова,1912). died(наталья_симонова,1998).

born(борис_кудияш,1941).   died(борис_кудияш,2016).
born(светлана_кудияш,1949).

born(митя_кудияш,1939). died(митя_кудияш,2013).
born(алена_кудияш,1948).

born(алексей_бутанин,1920). died(алексей_бутанин,1989).
born(кира_ершова,1924).          died(кира_ершова,2009).
born(виктор_бутанин,1946).
born(светлана_бутанина,1945).

born(михаил_кудияш,1979).
born(елена_кудияш,1979).
born(я,2005).
born(мария_кудияш,2012).
born(александр_кудияш,1976). died(александр_кудияш,2024).

born(алексей_васько,1888). died(алексей_васько,1965).
born(надежда_алексеева,1888). died(надежда_алексеева,1964).
born(андрей_васько,1915).  died(андрей_васько,1999).

born(дмитрий_ляхов,1885). died(дмитрий_ляхов,1966).
born(любовь_громова,1885). died(любовь_громова,1963).
born(надежда_васько,1916). died(надежда_васько,2002).

born(галина_васько,1951).
born(наталья_васько,1948).
born(сергей_фоменко,1944).

/* === БРАКИ/РАЗВОДЫ (ЗАГС) === */
married(юрий_кудияш, александра_мелько, 1911).
married(михаил_симонов, алла_симонова, 1910).
married(кирилл_панко, лидия_норрис, 1938).

married(антон_кудияш, наталья_симонова, 1937).

married(борис_кудияш, светлана_кудияш, 1970).

married(алексей_бутанин, кира_ершова, 1946).
married(виктор_бутанин, светлана_бутанина, 1966).

married(михаил_кудияш, елена_кудияш, 2010).

married(алексей_васько, надежда_алексеева, 1910).
married(андрей_васько, надежда_васько, 1935).

married(дмитрий_ляхов, любовь_громова, 1917).

married(сергей_фоменко, наталья_васько, 1966).
divorced(сергей_фоменко, наталья_васько, 2013).

/* === РОДИТЕЛЬСТВО (по стрелкам на схеме) === */

% родители Антона
parent(юрий_кудияш, антон_кудияш).
parent(александра_мелько, антон_кудияш).

% родители Натальи (1912)
parent(михаил_симонов, наталья_симонова).
parent(алла_симонова, наталья_симонова).

% родители Светланы (1949)
parent(кирилл_панко, светлана_кудияш).
parent(лидия_норрис, светлана_кудияш).

% дети Антона и Натальи: Борис, Мария, Александр(1939)
parent(антон_кудияш, борис_кудияш).
parent(наталья_симонова, борис_кудияш).
parent(антон_кудияш, алена_кудияш).
parent(наталья_симонова, алена_кудияш).
parent(антон_кудияш, митя_кудияш).
parent(наталья_симонова, митя_кудияш).

% дети Бориса и Светланы (1970): Михаил и Александр(1976)
parent(борис_кудияш, михаил_кудияш).
parent(светлана_кудияш, михаил_кудияш).
parent(борис_кудияш, александр_кудияш).
parent(светлана_кудияш, александр_кудияш).

% родители Виктора
parent(алексей_бутанин, виктор_бутанин).
parent(кира_ершова, виктор_бутанин).

% родители Светланы Бутаниной
parent(андрей_васько, светлана_бутанина).
parent(надежда_васько, светлана_бутанина).

% дети Андрея и Надежды Васько: Галина и Наталья
parent(андрей_васько, галина_васько).
parent(надежда_васько, галина_васько).
parent(андрей_васько, наталья_васько).
parent(надежда_васько, наталья_васько).

parent(алексей_васько, андрей_васько).
parent(надежда_алексеева, андрей_васько).

parent(дмитрий_ляхов, надежда_васько).
parent(любовь_громова, надежда_васько).

% дети Михаила и Елены: Я и Мария(2012)
parent(михаил_кудияш, я).
parent(елена_кудияш, я).
parent(михаил_кудияш, мария_кудияш).
parent(елена_кудияш, мария_кудияш).


/* =======================
   30 ПРАВИЛ
   ======================= */

% 1) alive_in/2 — жив в году Y (родился и не умер до/в Y)
alive_in(P, Y) :-
    born(P, B), B =< Y,
    \+ died_before_or_in(P, Y).

% 2) deceased_in/2 — уже умер к году Y
deceased_in(P, Y) :- died(P, D), D =< Y.

% 3) age_in/3 — возраст P в году Y (если жив или ещё не родился — не срабатывает)
age_in(P, Y, Age) :-
    born(P, B), B =< Y,
    ( died(P, D) -> End is min(D, Y) ; End is Y ),
    Age is End - B,
    Age >= 0.

% 4) older_than_in/3 — P старше Q в году Y
older_than_in(P, Q, Y) :-
    age_in(P, Y, AP), age_in(Q, Y, AQ), AP > AQ.

% 5) younger_than_in/3 — P младше Q в году Y
younger_than_in(P, Q, Y) :- older_than_in(Q, P, Y).

% симметричная проекция брака
married_sym(P1,P2,Y) :- married(P1,P2,Y).
married_sym(P1,P2,Y) :- married(P2,P1,Y).

% 6) spouse/3 — супруг(а) в году Y (в браке и он не прекращён смертью/разводом)
spouse(P1, P2, Y) :-
    married(P1, P2, M), M =< Y,
    \+ (divorced(P1, P2, D), D =< Y),
    \+ died_before_or_in(P1, Y),
    \+ died_before_or_in(P2, Y).

% 7) married_in/3 — люди в состоянии брака в Y
married_in(P1, P2, Y) :- spouse(P1, P2, Y).

% 8) divorced_in/2 — человек уже разведен к году Y
divorced_in(P, Y) :-
    divorced(P, _, D), D =< Y.
divorced_in(P, Y) :-
    divorced(_, P, D), D =< Y.

% 9)widowed_in(P, Y) :-
    married_sym(P, S, M), M =< Y,
    died(S, DS), DS =< Y,
    \+ (divorced(P, S, D), D =< DS).  % если развелись до смерти, не вдовец/вдова

% 10) single_in/2 — никогда не состоял(а) в браке до Y
single_in(P, Y) :-
    born(P, B), B =< Y,
    \+ (married(P, _, M), M =< Y),
    \+ (married(_, P, M2), M2 =< Y).

% min с "бесконечностью"
min_inf(inf, X, X) :- !.
min_inf(X, inf, X) :- !.
min_inf(A, B, M)  :- M is min(A, B).

% конец брака: мин(развод, смерть_P1, смерть_P2) или inf
marriage_end(P1, P2, End) :-
    (divorced(P1, P2, Dv) -> E1 = Dv ; E1 = inf),
    (died(P1, D1)         -> E2 = D1 ; E2 = inf),
    (died(P2, D2)         -> E3 = D2 ; E3 = inf),
    min_inf(E1, E2, T),
    min_inf(T,  E3, End).

% 11) marriage_interval/4 — начало=год ЗАГС, конец=marriage_end/3
marriage_interval(P1, P2, Start, End) :-
    married(P1, P2, Start),
    marriage_end(P1, P2, End).

% 12) married_in/3 — состоят в браке в году Y (уникальные пары)
married_in(P1, P2, Y) :-
    setof((A,B),
          ( marriage_interval(A, B, S, E),
            S =< Y,
            (E == inf ; Y =< E),
            A @< B),
          Pairs),
    member((P1,P2), Pairs).

% 13) marriage_duration/4 — длительность брака к Y
end_bound(Y, inf, Y).
end_bound(_, E,   E).

marriage_duration(P1, P2, Y, Duration) :-
    marriage_interval(P1, P2, S, E),
    end_bound(Y, E, End),
    End >= S,
    Duration is End - S.
% 14) ever_married/1 — когда-либо был(а) в браке
ever_married(P) :- married(P, _, _); married(_, P, _).

% 15) number_of_marriages/2 — число браков человека
number_of_marriages(P, N) :-
    setof(Pair-Start, (married(P, Q, Start), rpair(P, Q, Pair)), L), !,
    length(L, N).
number_of_marriages(_, 0).  % если браков нет

% 16) father/2 — отец
father(F, C) :- male(F), parent(F, C).

% 17) mother/2 — мать
mother(M, C) :- female(M), parent(M, C).

% 18) grandparent/2 — дед/бабушка
grandparent(G, C) :- parent(G, P), parent(P, C).

% 19) grandchild/2 — внук/внучка
grandchild(C, G) :- grandparent(G, C).

% 20) sibling/2 — родные брат/сестра (общий хотя бы один родитель)
sibling(A, B) :-
    parent(P, A), parent(P, B),
    A \= B.

% 21) full_sibling/2 — полнородные (оба родителя общие)
full_sibling(A, B) :-
    father(F, A), father(F, B),
    mother(M, A), mother(M, B),
    A \= B.

% 22) half_sibling/2 — неполнородные (ровно один общий родитель)
half_sibling(A, B) :-
    sibling(A, B),
    \+ full_sibling(A, B).

% 23) ancestor/2 — предок (транзитивно)
ancestor(A, D) :- parent(A, D).
ancestor(A, D) :- parent(A, X), ancestor(X, D).

% 24) descendant/2 — потомок
descendant(D, A) :- ancestor(A, D).

% 25) cousin/2 — двоюродные (родители — братья/сёстры)
cousin(A, B) :-
    parent(PA, A), parent(PB, B),
    sibling(PA, PB),
    A \= B.

% 26) uncle_aunt/2 — дядя/тётя (родной брат/сестра родителя)
uncle_aunt(U, N) :-
    parent(P, N),
    sibling(U, P).

% 27) nephew_niece/2 — племянник/племянница
nephew_niece(N, U) :- uncle_aunt(U, N).

% 28) step_parent/2 — отчим/мачеха: супруг родителя, не являющийся биородителем
step_parent(Step, C) :-
    parent(P, C),
    married(P, Step, M),
    \+ parent(Step, C),
    ( born(C, BC) -> M =< BC ; true ).

% 29) step_child/2 — пасынок/падчерица
step_child(C, Step) :- step_parent(Step, C).

% 30) born_in_marriage/1 — ребёнок рождён в браке (родители были женаты до/в год рождения)
born_in_marriage(C) :-
    father(F, C), mother(M, C),
    born(C, Yb),
    married(F, M, Ym), Ym =< Yb.

/* Вспомогательные предикаты */
died_before_or_in(P, Y) :- died(P, D), D =< Y.

% Нужен для number_of_marriages/2 (безориентированная пара)
rpair(X,Y,[A,B]) :- (X @< Y -> A=X, B=Y ; A=Y, B=X).