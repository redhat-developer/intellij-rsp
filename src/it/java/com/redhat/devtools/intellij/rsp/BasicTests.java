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
import static com.intellij.remoterobot.search.locators.Locators.byXpath;

import com.redhat.devtools.intellij.rsp.dialogs.ProjectStructureDialog;
import com.redhat.devtools.intellij.rsp.mainIdeWindow.RspToolFixture;
import com.redhat.devtools.intellij.rsp.mainIdeWindow.SingleHeighLabelFixture;
import com.redhat.devtools.intellij.rsp.tests.RunRspConnectorsTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.redhat.devtools.intellij.commonuitest.UITestRunner;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.information.TipDialog;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import com.redhat.devtools.intellij.commonuitest.utils.project.CreateCloseUtils;

import com.redhat.devtools.intellij.rsp.tests.CheckRspConnectorsExistsTest;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * JUnit UI tests for intellij-rsp
 *
 * @author olkornii@redhat.com
 */
public class
BasicTests {

    private static RemoteRobot robot;
    private static ComponentFixture rspViewTree;

    @BeforeAll
    public static void connect() {
        robot = UITestRunner.runIde(IntelliJVersion.COMMUNITY_V_2024_2, 8580);
        createEmptyProject();
        openRspServersTab();

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
        step("New Empty Project", () -> CheckRspConnectorsExistsTest.checkRspConnectors(rspViewTree));
    }

    @Test
    public void runRspConnectors() {
        step("Start/Stop RSP Connectors", () -> RunRspConnectorsTest.runRspServers(robot, rspViewTree));
    }

    private static void createEmptyProject(){
        CreateCloseUtils.createEmptyProject(robot, "test-project");
        ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
        closeTipDialogIfItAppears();
        closeGotItPopup();
        closeOpenedEditors();
    }

    private static boolean isRspViewTreeAvailable(ComponentFixture rspViewTree){
        List<RemoteText> allText = rspViewTree.findAllText();
        String firstText = allText.get(0).getText();
        return !"Nothing to show".equals(firstText);
    }

    private static void openRspServersTab(){
        waitFor(Duration.ofSeconds(30), Duration.ofSeconds(1), "The 'RSP Servers' stripe button is not available.", () -> {
            try {
                robot.find(ComponentFixture.class, byXpath("//div[@accessiblename='RSP Servers']"), Duration.ofSeconds(1));
                return true;
            } catch (WaitForConditionTimeoutException e) {
                return false;
            }
        });
        robot.find(ComponentFixture.class, byXpath("//div[@accessiblename='RSP Servers']"), Duration.ofSeconds(5)).click();
    }

    public static void closeTipDialogIfItAppears() {
        try {
            TipDialog tipDialog = robot.find(TipDialog.class, Duration.ofSeconds(10));
//            tipDialog.close(); // temporary commented
            robot.find(ComponentFixture.class, byXpath("//div[@accessiblename='Close' and @class='JButton' and @text='Close']"), Duration.ofSeconds(5)).click(); // temporary workaround
        } catch (WaitForConditionTimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void closeGotItPopup() {
        try {
            robot.find(ComponentFixture.class, byXpath("GotIt Popup", "//div[@accessiblename='Got It' and @class='JButton' and @text='Got It']"), Duration.ofSeconds(10)).click();
        } catch (WaitForConditionTimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void closeOpenedEditors() {
        List<SingleHeighLabelFixture> singleHeighLabelsList = robot.findAll(SingleHeighLabelFixture.class, byXpath("//div[@class='SingleHeightLabel']"));
        System.out.println("Next opened editors will be closed: " + singleHeighLabelsList);
        for (SingleHeighLabelFixture singleHeighLabel : singleHeighLabelsList) {
            singleHeighLabel.find(ComponentFixture.class, byXpath("//div[@accessiblename='Close. Alt-Click to Close Others (Ctrl+F4)' and @class='InplaceButton']")).click();
        }
    }
}
