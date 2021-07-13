Slightly more detailed overview of the Minima Protocol

At it's core - Minima is a PoW blockchain using a UTXO data model.

Users mine transactions and are required to find a certain difficulty (specified by the network) to be considered valid. This equates to about 10 seconds work.

The chain is built using a mechanism similar to P2Pool - Users are trying to find a much smaller difficulty hash/nonce TxPoW unit but 1 TxPoW unit every 20 seconds is also a block - this is the block difficulty calculated by the network in a similar way to normal chains. As more users are on the network, this difficulty increases so less blocks are found.

Every user runs a full validating node. Every user helps build the chain.

The database where the UTXO set is stored is an MMR - where every user only keeps the entries and proofs for their coins. They provide proofs for each UTXO when they want to spend - add them as inputs to a txn.

The actual construction of the MMR is my own. And uses - imho - a nice system to allow for reorgs and coin proof computation without having to reconstruct the whole MMR from scratch.

Each block has an 'MMRSet' - I think of it as a slice actually. This is the MMR Peaks from the previous block, all the spends+proofs and all the new coins created in that block. This is a static immutable deterministic data structure that you only need to compute once per block. It cannot change.

Then.. when you want to find or check a proof.. you stack all the slices on top of each other and look down through all the slices. And this is your MMR DB. So since I know the current TIP block, I just getParent, getParent, getParent etc etc.. and take the entry for the most recent block. This is the proof.

We also use a cascading PoW chain.. which means.. each block not only points to it's parent, but to it's Super Parents. This is not a new idea - it was bouncing around BitcoinTalk about ~2012 i think ( Socrates called it the Super Hash Highway ). I think the best explanation I saw of this was the PoPoW paper - Proof of Proof of Work ( they wanted to use it for side-chains ) but this was after I had made mine. The concept is the same.

Another interesting feature is our core scripting. Minima uses a more powerful language than Bitcoin ( honestly if they just added 5/6 functions you could do a lot more with BTC)- but it is still VERY simple. And since you can emulate a Turing machine - by definition it is Turing complete ( although you cannot run scripts forever of course ). We do not have a global state - that's a no no in my book - bloats everything.. but transactions do have a local Txn State, that is added to the MMR entry, and you can access these values in the next transaction. This gives a history mechanic that is really powerful and allows for lots of nice scripts. You can tell which number in a sequence of transactions you are - great for Eltoo.

I am really sorry I don't have a nice clean paper explaining everything in glorious detail - that is currently the code - but the protocol bit - not everything - is at what I consider an RC1 status.

So - tbh - I do expect there to be changes, but these will be fixes, I don't have any more to add. We are feature complete.
