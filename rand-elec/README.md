# rand-elec

This is a Rust crate to simulate randomized leader election algorithms. The details of these algorithms can be found in the slides on leader election.

## How to install Rust

The easiest way to install Rust is to use the [rustup](https://rustup.rs) installer. On MacOS, the installer can also be installed via [homebrew](https://brew.sh). The [official documention](https://www.rust-lang.org/tools/install) provides more details on how to install Rust.

## How to build/run the project

The simplest way to compile (as needed) and run the program is as follows. In a terminal window, change directory to `rand-elec` (same as this file) and run the following command:

~~~
cargo run --release -- help
~~~

The part after the `--` are the command-line arguments to the Rust program. In this case, this will display the usage information.
The `--` are not necessary in this specific case because there are no dashed flags addressed to the rust program.
The double dashes `--` are omitted in the other examples.

For instance, to simulate a signal run of the naive algorithm in a system with 15 processes, type the following command:

~~~
cargo run --release run naive 15
~~~

Alternatively, to find the median of 20 runs of the tournament algorithm in a system with 10000 processes, type the following:

~~~
cargo run --release stats 20 tournament 10000
~~~

## How to read the code

The parts relevant to the different algorithms are in the file `src/lib.rs`
In particular, the code of each of the algorithms are in the following functions (command line name -> function name):
* `naive` -> `naive_gen`
* `tournament` -> `tournament_gen`
* `biased` -> `biased_tournament_gen`
* `biased-fixed` -> `biased_fixed_gen`
* `biased-single` -> `biased_single_gen`
The state of a round (`RoundState`) is defined as a vector of Booleans. Given a round state, each of these functions computes the state of the next round. When a round state contains exactly one `true` value (and all else `false`), the process that proposed `true` becomes the leader.
