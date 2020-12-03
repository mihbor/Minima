#!/bin/bash

# Mandatory args
MARGS=1

BASEDIR="/Users/sh601/gitRepos/Minima"
#WORKINGDIR="${BASEDIR}/build/classes/java/main"
WORKINGDIR="${BASEDIR}/jar"
LIBDIR="${BASEDIR}/lib"

MASTERCMD="java -jar minima.jar -private -conf /tmp/minimaTest.master.${NOW}"
CLIENTCMD="java -jar minima.jar -connect 127.0.0.1 9001 -port"

BASEPORT="9001"
PORTINC=10
NUMCLIENTS=0
CLEAN=""

NOW=$(date +"%H%M-%m%d%Y")
LOG="${NOW}.log"
MASTERLOG="${BASEDIR}/master.${LOG}"

PIDS=""

usage () {

    echo "usage: $0 -n | --numclients x [-c | --clean]"
    echo "example: $0 -n 3 -c"
}

help () {

  echo "The script always starts a master on port 9001."
  echo "-n, --numclients [0-n] - The number of Minima clients to start."
  echo "-c, --clean - Whether to start afresh."
  echo "-h, --help - Prints this help"
}

cleanup () {

  for PID in "$PIDS"
  do
    kill -9 $PID
  done
}

startMain () {

  CMD="${MASTERCMD}"
  if [[ ! -z $CLEAN ]]
  then
    CMD="${CMD} ${CLEAN}"
  fi

  echo $CMD >${MASTERLOG}
  eval "${CMD} >>${MASTERLOG} 2>&1 &"
  PIDS=$!
}

startClients () {

  if [[ $NUMCLIENTS -gt 0 ]]
  then

    INC=10
    for (( i=1; i<=${NUMCLIENTS}; i++ ))
    do

      CLIENTLOG="${BASEDIR}/client${i}.${LOG}"
      CLIENTCONF="-conf /tmp/minimaTest.client${i}.${NOW}"
      let OFFSET=${PORTINC}*i
      let PORT=${BASEPORT}+${OFFSET}

      CMD="${CLIENTCMD} ${PORT} ${CLIENTCONF}"
      if [[ ! -z $CLEAN ]]
      then
        CMD="${CMD} ${CLEAN}"
      fi

      echo $CMD >${CLIENTLOG}
      eval "${CMD} >>${CLIENTLOG} 2>&1 &"
      PID=$!
      PIDS="${PIDS} ${PID}"
    done
  fi

}

if [[ $# -ge $MARGS ]]
then

  while [ $# -gt 0 ]
  do

    case $1 in

        -n | --numservers ) NUMCLIENTS=$2; shift 2;;
        -c | --clean ) CLEAN="-clean"; shift;;
        -h | --help ) help; exit;;
        * ) usage; exit 1;;

    esac

  done

else

  usage
  exit 1

fi

trap cleanup EXIT

DIR=$(pwd)
cd ${WORKINGDIR}

startMain
startClients

read -p "Press Return to Cleanup and Close..."

cd ${DIR}
exit
