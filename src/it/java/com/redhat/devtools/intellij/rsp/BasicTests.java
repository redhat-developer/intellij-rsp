/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.rsp;


import com.intellij.remoterobot.RemoteRobot;

import java.time.Duration;
import java.util.List;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.rsp.dialogs.ProjectStructureDialog;
import com.redhat.devtools.intellij.rsp.mainIdeWindow.RspToolFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.redhat.devtools.intellij.commonUiTestLibrary.UITestRunner;
import com.redhat.devtools.intellij.commonUiTestLibrary.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonUiTestLibrary.fixtures.dialogs.projectManipulation.NewProjectDialog;
import com.redhat.devtools.intellij.commonUiTestLibrary.fixtures.mainIdeWindow.toolWindowsPane.ToolWindowsPane;
import com.redhat.devtools.intellij.commonUiTestLibrary.fixtures.dialogs.information.TipDialog;
import com.redhat.devtools.intellij.commonUiTestLibrary.fixtures.mainIdeWindow.ideStatusBar.IdeStatusBar;

import com.redhat.devtools.intellij.rsp.tests.CheckRspConnectorsExists;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * JUnit UI tests for intellij-rsp
 *
 * @author olkornii@redhat.com
 */
public class BasicTests {

    private static RemoteRobot robot;
    private static ComponentFixture rspViewTree;

    @BeforeAll
    public static void connect() {
        robot = UITestRunner.runIde(UITestRunner.IdeaVersion.V_2020_2, 8580);
        createEmptyProject();
        final ToolWindowsPane toolWindowsPane = robot.find(ToolWindowsPane.class);
        waitFor(Duration.ofSeconds(15), Duration.ofSeconds(1), "The 'RSP servers' stripe button is not available.", () -> isStripeButtonAvailable(toolWindowsPane, "RSP Servers"));
        toolWindowsPane.stripeButton("RSP Servers").click();

        RspToolFixture rspToolFixture = robot.find(RspToolFixture.class);
        rspViewTree = rspToolFixture.getRspViewTree();
        waitFor(Duration.ofSeconds(15), Duration.ofSeconds(1), "RSP Tree View is not available.", () -> isRspViewTreeAvailable(rspViewTree));
    }

    @AfterAll
    public static void closeIde() {
        UITestRunner.closeIde();
    }

    @Test
    public void checkRspConnectorsExists() {
        step("New Empty Project", () -> CheckRspConnectorsExists.checkRspConnectors(rspViewTree));
    }

    private static void createEmptyProject(){
        final FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class);
        flatWelcomeFrame.createNewProject();
        final NewProjectDialog newProjectDialogFixture = flatWelcomeFrame.find(NewProjectDialog.class, Duration.ofSeconds(20));
        newProjectDialogFixture.selectNewProjectType("Empty Project");
        newProjectDialogFixture.next();
        newProjectDialogFixture.finish();

        final IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class);
        ideStatusBar.waitUntilProjectImportIsComplete();
        TipDialog.closeTipDialogIfItAppears(robot);
        ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
        ideStatusBar.waitUntilAllBgTasksFinish();
    }

    private static boolean isStripeButtonAvailable(ToolWindowsPane toolWindowsPane, String label) {
        try {
            toolWindowsPane.stripeButton(label);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }

    private static boolean isRspViewTreeAvailable(ComponentFixture rspViewTree){
        List<RemoteText> allText = rspViewTree.findAllText();
        String firstText = allText.get(0).getText();
        return !"Nothing to show".equals(firstText);
    }
}
