use clap::{Parser,Subcommand,ValueEnum};
use indicatif::ProgressBar;

use rand_elec::*;


#[derive(Parser, Debug)]
#[clap(author, version, about, long_about = None)]
struct Args {
    #[clap(subcommand)]
    command: Commands,
}

#[derive(Debug, Copy, Clone, PartialEq, Eq, PartialOrd, Ord, ValueEnum)]
enum Mode {
    Naive,
    Tournament,
    Biased,
    BiasedSingle,
    BiasedFixed,
}

#[derive(Subcommand, Debug)]
enum Commands {
    /// Execute a single simulation run.
    Run(RunArgs),

    /// Estimate the expected number of rounds to obtain a leader.
    Stats(StatsArgs)
}

#[derive(clap::Args, Debug)]
struct StatsArgs {
    /// number of repetitions for statistics.
    #[clap()]
    repetitions: usize,

    /// mode of the randomized selection.
    #[clap(value_enum)]
    mode: Mode,

    /// number of anonymous processes in the modelled system.
    #[clap()]
    size: usize,

    /// enables display of each run.
    #[clap(short = 'v', long = "verbose" )]
    verbose: bool,
}

#[derive(clap::Args, Debug)]
struct RunArgs {
    /// mode of the randomized selection.
    #[clap(value_enum)]
    mode: Mode,

    /// number of anonymous processes in the modelled system.
    #[clap()]
    size: usize,
}


fn generator_for(mode: Mode) -> Generator {
    match mode {
        Mode::Naive => naive_gen,
        Mode::Tournament => tournament_gen,
        Mode::Biased => biased_tournament_gen,
        Mode::BiasedSingle => biased_single_gen,
        Mode::BiasedFixed => biased_fixed_gen,
    }
}


fn median(data: Vec<usize>) -> f64 {
    if data.len() % 2 == 1 {
        data[data.len() / 2] as f64
    } else {
        let mid_i = data.len() / 2;
        let mid_hi = data[mid_i] as f64;
        let mid_lo = data[mid_i - 1] as f64;
        (mid_hi + mid_lo) / 2. 
    }
}

fn main() {
    let args = Args::parse();

    //println!("{:?}", args);

    match args.command {
        Commands::Run(RunArgs{ mode, size }) => {
            let generator: Generator = generator_for(mode);
            let hist = single_run(size, generator);
            for (rnd, count) in hist.iter().enumerate() {
                println!("{rnd:6}: {count:6}");
            }
        }
        Commands::Stats(StatsArgs{ repetitions, mode, size, verbose }) => {
            let generator: Generator = generator_for(mode);
            let mut data = Vec::<usize>::with_capacity(repetitions);

            let pb = ProgressBar::new(repetitions as u64);

            for _ in 0..repetitions {
                let hist = single_run(size, generator);
                pb.inc(1);
                data.push(hist.len());
            }
            data.sort_unstable();

            pb.finish_with_message("done");

            if verbose {
                for (i, rnds) in data.iter().enumerate() {
                    println!("{i:3}: {rnds:6}");
                }
                println!("--------------------");
            }
            println!("median: {} rounds (rep: {})", median(data), repetitions);
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_median() {
        assert_eq!(median(vec![1,2,3,3,4,5]), 3.);
        assert_eq!(median(vec![1]), 1.);
        assert_eq!(median(vec![1,2,3,4,5,6]), 3.5);
        assert_eq!(median(vec![1,2,3,4,5,6,7]), 4.);
    }
}
