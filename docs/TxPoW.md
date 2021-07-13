The TxPoW unit - the basic building block of Minima.

A TxPoW unit is more than just a txn. It has all the details required to be considered a block. Very few txpow become blocks, but in the event that they do all this information is obviously required.

A TxPoW unit has a header and a body.

The Header has :

* Time milli - the time this txpow was created.
* Block Number - IF this unit becomes a block what block is it.
* Parent Blocks - not only do you point to your immediate previous block but to each block at each Super level. Used for cascading.
* MMR Details - this includes the root MMR hash ( so you can prove coins existed with a proof and just the header ) and the total coins in the system ( we use a hash sum tree so the total amount of Minima is known every block - no inflation bugs possible )
* TxBody Hash - the hash of the TxPow Body.
* Block Difficulty - this is the difficulty required for this unit to be considered a valid block.
* Nonce - and of course the nonce that is cycled through to change the hash and to prove the work..

The Body Has :

* Txn Difficulty - the required difficulty for this unit to be a valid txn. This is the value that all users try to achieve when cycling through nonce values.
* A Transaction - inputs, outputs, state variables
* A Witness - Signatures, Script (The script for the various P2SH addresses used), Tokens (The tokens used in the transaction), and the MMR proofs that point to a valid unspent MMR entry in the past 24 hours for each input coin used in the txn.
* Txn List - this is a list of the hash of the txns that are added to this block. Minima uses compact blocks. Only the hash of txns is added - since txns are already sent across the network.

There is a bit more sticky tape but that is about it. ( There is an extra transaction called the burn txn and witness and this is useful for any Layer 2 Eltoo TxPoW you wish to post at a later date )

Once you have constructed this object.. you hash the header, check the hash is LESS than the Txn Difficulty, and if not you increment the nonce and hash it again. Rinse repeat.

You do not care about the block difficulty. You only check the txn difficulty. Once you have crossed that threshold the TxPoW is valid and you send it out to the network.

How are blocks created ? Imagine I ask you to roll a dice that has a value from 0-1 and I say you need to roll less than 0.5. On average this will take 2 rolls - but how low will the actual number you roll be ? Well 1 in every 10 rolls will be less than 0.1 and one in every 100 rolls will be less than 0.01 - on average. This is how P2Pool works and this is how the Minima network calculates the eventual block winners. By chance. There is no reward paid out for finding a block. You are rewarded by your txn being accepted by the network. BUT - 1 TxPoW unit every 20 seconds.. has a very low difficulty, lower than the required block difficulty, and that is added to the chain.

How are txns chosen to be put in blocks by users ? The Burn. This is same as Bitcoin, the outputs are less than the inputs but there is no coinbase txn to reallocate those funds. They are burnt. Those who burn more, get chosen first, as the total supply of Minima goes down and benefits everyone.

Eventually - you can get rid of the Body - and only keep the Headers. They store the PoW. Using the cascading super blocks you have a store of PoW that does not grow. Logarithmic in size.