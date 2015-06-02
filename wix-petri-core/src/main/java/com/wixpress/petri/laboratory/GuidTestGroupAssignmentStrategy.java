package com.wixpress.petri.laboratory;

/**
 * This strategy is responsible for two things: creating a test group assignment according to userGuid.
 * This function is consistent and will give the same results when user changes browsers.
 * The other thing that it does is replacing the assignments of linked tests
 *
 * @author alex
 * @since 8/18/11 4:45 PM
 */
public class GuidTestGroupAssignmentStrategy extends TestGroupAssignmentStrategy {

    @Override
    protected int getToss(com.wixpress.petri.experiments.domain.Experiment experiment, String kernel) {
        return Math.abs(experiment.getSeed() * new StringBuilder(kernel).toString().hashCode()) % 10000;
    }

}


