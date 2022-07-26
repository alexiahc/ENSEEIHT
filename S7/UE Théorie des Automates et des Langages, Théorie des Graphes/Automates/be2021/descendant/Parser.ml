open Tokens

(* Type du résultat d'une analyse syntaxique *)
type parseResult =
  | Success of inputStream
  | Failure
;;

(* accept : token -> inputStream -> parseResult *)
(* Vérifie que le premier token du flux d'entrée est bien le token attendu *)
(* et avance dans l'analyse si c'est le cas *)
let accept expected stream =
  match (peekAtFirstToken stream) with
    | token when (token = expected) ->
      (Success (advanceInStream stream))
    | _ -> Failure
;;

(* Définition de la monade  qui est composée de : *)
(* - le type de donnée monadique : parseResult  *)
(* - la fonction : inject qui construit ce type à partir d'une liste de terminaux *)
(* - la fonction : bind (opérateur >>=) qui combine les fonctions d'analyse. *)

(* inject inputStream -> parseResult *)
(* Construit le type de la monade à partir d'une liste de terminaux *)
let inject s = Success s;;

(* bind : 'a m -> ('a -> 'b m) -> 'b m *)
(* bind (opérateur >>=) qui combine les fonctions d'analyse. *)
(* ici on utilise une version spécialisée de bind :
   'b  ->  inputStream
   'a  ->  inputStream
    m  ->  parseResult
*)
(* >>= : parseResult -> (inputStream -> parseResult) -> parseResult *)
let (>>=) result f =
  match result with
    | Success next -> f next
    | Failure -> Failure
;;

let acceptIdent stream = 
  (match (peekAtFirstToken stream) with
  | (UL_Ident _) -> (Success (advanceInStream stream))
  | _ -> Failure

(* parseMachine : inputStream -> parseResult *)
(* Analyse du non terminal Programme *)
let (* rec *) parseR stream =
  (print_string "R -> ...");
  (match (peekAtFirstToken stream) with
    |UL_MODEL -> inject stream >>=
                accept UL_MODEL >>=
                acceptIdent >>= 
                accept UL_ACCOUV >>=
                parseSE >>=
                accept UL_ACCFER 
    | _ -> Failure)



(* POUR LE PARSER 
MATCH AVEC LES ID QUI SONT A GAUCHE DU TEXTE 
INJECT STREAM 
ACCEPT UL SI PAS ID OU NUM
ACCEPTID ET ACCEPTNUM A DEFINIR COMME FONCTION 
POUR VERIF TOUS LES ELEMENTS QUI SONT DEDANS (LISTE D'ELEMENT) AVEC FONCTION DONNEE *)

