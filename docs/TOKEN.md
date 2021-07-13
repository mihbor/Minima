TOKENS

Tokens have proven themselves to be invaluable assets in the crypto space. Whatever reservations one may have about the ERC20 standard it has born innumerable fruit.

Minima has tokens natively. We use coloured coins. Every Coin in Minima has 5 attributes :

1) Address - the hash of the script. All addresses are P2SH in effect.
2) Amount - the amount of 'Minima'. Even token transactions are just amounts of coloured Minima.
3) CoinID - the globally unique identifier for a coin. No 2 coins have the same coinid. This is simply because the coinid is hash(txnhash|output_num_in_txn) and txn hash is always unique as it includes all the input coins.
4) TokenID - the tokenId. Minima is 0x00. Everything else has a full 64 byte hash. Tokens are created by colouring Minima.
5) PREVSTATE - these are the state variables(0-255) of the transaction this output was created in. You can access this data from scripts.

The Token is specified by other characteristics.
1) Token Name/Description - a String description that can be just a name or a full JSON.
2) Minima Amount - the total amount of Minima coloured to be this token.
3) Token Amount - the total number of these tokens. If you colour.. 0.0000001 Minima.. is that 10 tokens with 34 decimal places.. or 1000 tokens with 32.
4) Token Script - Each token has a separate script that must also return TRUE when attempting to spend a UTXO.
   For instance this could be 'make sure 1% is sent to this address, for a charity coin,
   RETURN VERIFYOUT(@INPUT CHARITY_ADDRESS @AMOUNT*0.01 @TOKENID)

   or a counter mechanism that checks a counter has been incremented.
   RETURN STATE(99) EQ INC(PREVSTATE(99))

   Both the address script and the Token script must return TRUE. A token by default has RETURN TRUE as it's script.

The TokenID is the hash of all these details AND the CoinID of the Minima coin used to make the token. Each TokenID is globally unique.

This token structure is added to any transaction wishing to use that token so every user can know how many, what scripts, name etc of the Token is correct and valid.
