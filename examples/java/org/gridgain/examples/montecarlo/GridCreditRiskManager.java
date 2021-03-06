// Copyright (C) GridGain Systems Licensed under GPLv3, http://www.gnu.org/licenses/gpl.html

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.examples.montecarlo;

import org.gridgain.examples.montecarlo.api20.*;
import org.gridgain.grid.gridify.*;
import org.gridgain.grid.typedef.*;
import java.util.*;

/**
 * This class abstracts out the calculation of risk for a credit portfolio.
 *
 * @author 2012 Copyright (C) GridGain Systems
 * @version 4.0.0c.22032012
 */
@SuppressWarnings({"FloatingPointEquality"})
public class GridCreditRiskManager {
    /**
     * Default randomizer with normal distribution.
     * Note that since every JVM on the grid will have its own random
     * generator (independently initialized) the Monte-Carlo simulation
     * will be slightly skewed when performed on the grid due to skewed
     * normal distribution of the sub-jobs comparing to execution on the
     * local node only with single random generator. Real-life applications
     * may want to provide its own implementation of distributed random
     * generator.
     */
    private static Random rndGen = new Random();

    /**
     * Calculates credit risk for a given credit portfolio. This calculation uses
     * Monte-Carlo Simulation to produce risk value.
     * <p>
     * Note that this class generally represents a business logic and the entire
     * grid enabling occurs in one line of annotation added to this method:
     * <pre name="code" class="java">
     * ...
     * &#64;Gridify(taskClass = GridCreditRiskGridTask.class)
     * ...
     * </pre>
     * Note also that this annotation could have been added externally via XML
     * file leaving this file completely untouched - yet still fully grid enabled.
     *
     * @param portfolio Credit portfolio.
     * @param horizon Forecast horizon (in days).
     * @param num Number of Monte-Carlo iterations.
     * @param percentile Cutoff level.
     * @return Credit risk value, i.e. the minimal amount that creditor has to
     *      have available to cover possible defaults.
     */
    @Gridify(taskClass = GridCreditRiskGridTask.class)
    public double calculateCreditRiskMonteCarlo(GridCredit[] portfolio, int horizon, int num, double percentile) {
        X.println(">>> Calculating credit risk for portfolio [size=" + portfolio.length + ", horizon=" +
            horizon + ", percentile=" + percentile + ", iterations=" + num + "] <<<");

        long start = System.currentTimeMillis();

        double[] losses = calculateLosses(portfolio, horizon, num);

        Arrays.sort(losses);

        double[] lossProbs = new double[losses.length];

        // Count variational numbers.
        // Every next one either has the same value or previous one plus probability of loss.
        for (int i = 0; i < losses.length; i++)
            if (i == 0)
                // First time it's just a probability of first value.
                lossProbs[i] = getLossProbability(losses, 0);
            else if (losses[i] != losses[i - 1])
                // Probability of this loss plus previous one.
                lossProbs[i] = getLossProbability(losses, i) + lossProbs[i - 1];
            else
                // The same loss the same probability.
                lossProbs[i] = lossProbs[i - 1];

        // Count percentile.
        double crdRisk = 0;

        for (int i = 0; i < lossProbs.length; i++)
            if (lossProbs[i] > percentile) {
                crdRisk = losses[i - 1];

                break;
            }

        X.println(">>> Finished calculating portfolio risk [risk=" + crdRisk +
            ", time=" + (System.currentTimeMillis() - start) + "ms]");

        return crdRisk;
    }

    /**
     * Calculates losses for the given credit portfolio using Monte-Carlo Simulation.
     * Simulates probability of default only.
     *
     * @param portfolio Credit portfolio.
     * @param horizon Forecast horizon.
     * @param num Number of Monte-Carlo iterations.
     * @return Losses array simulated by Monte Carlo method.
     */
    private double[] calculateLosses(GridCredit[] portfolio, int horizon, int num) {
        double[] losses = new double[num];

        // Count losses using Monte-Carlo method. We generate random probability of default,
        // if it exceeds certain credit default value we count losses - otherwise count income.
        for (int i = 0; i < num; i++)
            for (GridCredit crd : portfolio) {
                int remDays = Math.min(crd.getRemainingTerm(), horizon);

                if (rndGen.nextDouble() >= 1 - crd.getDefaultProbability(remDays))
                    // (1 + 'r' * min(H, W) / 365) * S.
                    // Where W is a horizon, H is a remaining crediting term, 'r' is an annual credit rate,
                    // S is a remaining credit amount.
                    losses[i] += (1 + crd.getAnnualRate() * Math.min(horizon, crd.getRemainingTerm()) / 365)
                        * crd.getRemainingAmount();
                else
                    // - 'r' * min(H,W) / 365 * S
                    // Where W is a horizon, H is a remaining crediting term, 'r' is a annual credit rate,
                    // S is a remaining credit amount.
                    losses[i] -= crd.getAnnualRate() * Math.min(horizon, crd.getRemainingTerm()) / 365 *
                        crd.getRemainingAmount();
            }

        return losses;
    }

    /**
     * Calculates probability of certain loss in array of losses.
     *
     * @param losses Array of losses.
     * @param i Index of certain loss in array.
     * @return Probability of loss with given index.
     */
    private double getLossProbability(double[] losses, int i) {
        double cnt = 0;
        double loss = losses[i];

        for (double tmp : losses)
            if (loss == tmp)
                cnt++;

        return cnt / losses.length;
    }
}
