# Perfect-play Blackjack Analysis and Simulator

## History
I wrote this program a few years back for two reasons:

1. I had a few friends who were convinced they could 'beat' the system, and this gave them the ability to simulate a game without risking real money, and
1. It involves various advanced and elegant algorithms from Computer Science I was working on at the time, including:
  1. Alpha-beta minimax adversarial search algorithms
  1. Hash-based nth-order(1) Transposition tables

## TODO

1. While the algorithms are provably correct, the code could do with a clean-up and refactor.
1. The view layer is enforced via a UI interface. A console text-based interface is included but a graphical UI implementation would be nice.


## Theoretical Overview

At each point in a game of Blackjack the system has a specific state. From each state there is a range of choices leading to other states.

This can be visualised as a tree of nodes and branchs, where each node is a state, and the branches are different choices.

The tree leaves are points in the game where a player has won or lost a hand. That is, the Return-on-Investment is known (either 0 or 1);

Consider a node a single level about the leaves. Each leaf is connected via a branch with some associated probability, which we can calculate knowing what cards are left in the deck.

In this way, we can determine the value of all nodes.

The problem then becomes that there are billions of billions of nodes we would need to calculate, making this approach computationally unfeasable.

To overcome this problem we must use tree-pruning and a hash-table with a bit of cleverness. The clever part is recognising that there are many nodes in the tree that are equivalent. If we have calculated a node, we do not need to re-calculate its equivalents, we can simply prune the sub-tree below and insert the previously calculated value.

For example, consider the following:

1. Player is dealt Ace, King, Queen
1. Player is dealt Ace, Queen, King
1. Player is dealt King, Ace, Queen
1. Player is dealt King, Queen, Ace
1. Player is dealt Queen, Ace, King
1. Player is dealt Queen, King, Ace

These nodes are all equivalent. If we known the ROI of the first point is 0.565, we can avoid re-calculating the next 5 nodes. The larger the tree, the more efficient this becomes.

In this way, we reduce calculation time from billions of years to 10s of milliseconds!!


## Usage
1. Ensure you have the JDK installed
1. `mkdir /your-target-directory`
1. `cd /your-target-directory`
1. `git clone git@github.com:andrewryantech/perfect-play-blackjack.git .`
1. `javac ryan/blackjack/view/CommandLineUI.java`
1. `java ryan.blackjack.view.CommandLineUI`
