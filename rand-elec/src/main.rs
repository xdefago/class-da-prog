use clap::{Parser, Subcommand, ValueEnum};
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
    Stats(StatsArgs),
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
    #[clap(short = 'v', long = "verbose")]
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

fn main() {
    let args = Args::parse();

    //println!("{:?}", args);

    match args.command {
        Commands::Run(RunArgs { mode, size }) => {
            let generator: Generator = generator_for(mode);
            let hist = single_run(size, generator);
            for (rnd, count) in hist.iter().enumerate() {
                let eureka = if *count == 1 {
                    "** Leader Elected!! **"
                } else {
                    ""
                };
                println!("{rnd:6}: {count:6} {eureka}");
            }
        }
        Commands::Stats(StatsArgs {
            repetitions,
            mode,
            size,
            verbose,
        }) => {
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
            println!("median: {} rounds (rep: {})", median(&data), repetitions);
            if let Some((lo, hi)) = median_ci(&mut data, 0.95) {
                println!("95% ci: [{}, {}]", lo, hi);
            }
        }
    }
}

fn z_value(confidence: f64) -> f64 {
    assert!(confidence > 0. && confidence < 1.);
    use statrs::distribution::ContinuousCDF;
    use statrs::distribution::Normal;
    let alpha = 1. - confidence;
    let n = Normal::new(0., 1.).unwrap();
    n.inverse_cdf(1. - alpha / 2.)
}

/// compute the confidence interval for the median.
/// assumes the data is sorted.
/// returns None if the data set is too small (<3 items).
fn median_ci(data: &[usize], confidence: f64) -> Option<(usize, usize)> {
    assert!(confidence > 0. && confidence < 1.);
    let len = data.len();
    if len < 3 {
        return None;
    }

    let z = z_value(confidence);
    let q = 0.5; /* median */
    let n = len as f64;
    let mid_span = z * f64::sqrt(n * q * (1. - q));
    let lo = 1.max(f64::ceil(n * q - mid_span) as usize) - 1;
    let hi = (len - 1).min(f64::ceil(n * q + mid_span) as usize - 1);
    Some((data[lo], data[hi]))
}

fn median(data: &[usize]) -> f64 {
    if data.len() % 2 == 1 {
        data[data.len() / 2] as f64
    } else {
        let mid_i = data.len() / 2;
        let mid_hi = data[mid_i] as f64;
        let mid_lo = data[mid_i - 1] as f64;
        (mid_hi + mid_lo) / 2.
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_median() {
        assert_eq!(median(&[1, 2, 3, 3, 4, 5]), 3.);
        assert_eq!(median(&[1]), 1.);
        assert_eq!(median(&[1, 2, 3, 4, 5, 6]), 3.5);
        assert_eq!(median(&[1, 2, 3, 4, 5, 6, 7]), 4.);
    }

    #[test]
    fn test_median_ci() {
        assert_eq!(median_ci(&[1, 2, 3, 3, 4, 5], 0.9), Some((1, 5)));
        assert_eq!(median_ci(&[1], 0.9), None);
        assert_eq!(median_ci(&[1, 2, 3, 4, 5, 6], 0.9), Some((1, 6)));
        assert_eq!(median_ci(&[1, 2, 3, 4, 5, 6, 7], 0.9), Some((2, 6)));
        assert_eq!(
            median_ci(
                &[ 8, 11, 12, 13, 15, 17, 19, 20, 21, 21, 22, 23, 25, 26, 28 ],
                0.95
            ),
            Some((13, 23))
        );
    }
    #[test]
    fn test_stats() {
        use statrs::distribution::ContinuousCDF;
        use statrs::distribution::Normal;
        let n = Normal::new(0., 1.).unwrap();
        assert_eq!(n.inverse_cdf(0.975), z_value(0.95));
    }
}
