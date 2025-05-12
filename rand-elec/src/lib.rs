use rand::random;

/// global state of the system at a round. A process only holds `true` if it is a candidate
/// or `false` if it isn't.
pub type RoundState = Vec<bool>;

/// general type signature of a function which, given the state of a round generates
/// the state for the next round.
pub type Generator = fn(state: &RoundState) -> RoundState;

/// given the state of a round, count the number of processes with a value of `true` in
/// that round.
pub fn count_true(state: &RoundState) -> usize {
    state.iter().filter(|&&x| x).count()
}

/// Naive algorithm:
/// at each round each process takes `true` with probability 1/2.
pub fn naive_gen(state: &RoundState) -> RoundState {
    let count_true = count_true(state);
    if count_true == 1 {
        return state.clone();
    }

    state.iter().map(|_| random::<bool>()).collect()
}

/// Tournament algorithm:
/// at each round, each process that had `true` in the previous
/// round keeps `true` with probability 1/2. If all processes had false in the previous
/// round, then all processes make a random choice again.
pub fn tournament_gen(state: &RoundState) -> RoundState {
    let count_true = count_true(state);
    if count_true == 1 {
        return state.clone();
    }

    let reset = count_true == 0;

    state
        .iter()
        .map(|&old| (reset || old) && random::<bool>())
        .collect()
}

/// Biased tournament algorithm:
/// same as the tournament algorithm, but a process selects
/// `true` with probability 1 / number of candidates, or
/// 1 / number of processes in case of a reset.
pub fn biased_tournament_gen(state: &RoundState) -> RoundState {
    let count_true = count_true(state);
    if count_true == 1 {
        return state.clone();
    }

    let reset = count_true == 0;

    state
        .iter()
        .map(|&old| {
            (reset && random::<f64>() < (1. / state.len() as f64))
                || (old && random::<f64>() < (1. / count_true as f64))
        })
        .collect()
}

/// Biased fixed algorithm:
/// same as the tournament algorithm, but a process selects
/// `true` with probability 1 / number of candidates, or
/// 1 / number of processes in case of a reset.
pub fn biased_fixed_gen(state: &RoundState) -> RoundState {
    let count_true = count_true(state);
    if count_true == 1 {
        return state.clone();
    }

    let reset = count_true == 0;

    state
        .iter()
        .map(|&old| (reset || old) && random::<f64>() < (1. / state.len() as f64))
        .collect()
}

/// Biased single algorithm:
/// a process selects `true` with probability 1 / number of processes.
pub fn biased_single_gen(state: &RoundState) -> RoundState {
    let count_true = count_true(state);
    if count_true == 1 {
        return state.clone();
    }

    state
        .iter()
        .map(|&_| random::<f64>() < (1. / state.len() as f64))
        .collect()
}

/// given a state size and a state generator, simulate an execution
/// until a single leader emerges and return the history as a vector of
/// counting the number of true in each round.
pub fn single_run(size: usize, gen: Generator) -> Vec<usize> {
    let mut state = vec![true; size] as RoundState;
    let next_state: Generator = gen;
    let mut hist = vec![];

    loop {
        let count_true = count_true(&state);
        hist.push(count_true);
        if count_true == 1 {
            break;
        }
        state = next_state(&state);
    }
    hist
}
