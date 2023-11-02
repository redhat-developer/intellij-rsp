/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.rsp.tests;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.JPopupMenuFixture;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import com.redhat.devtools.intellij.rsp.mainIdeWindow.RightClickMenu;
import org.assertj.swing.core.MouseButton;

import java.time.Duration;

import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * @author olkornii@redhat.com
 */
public class RunRspConnectorsTest extends AbstractRspServersTest {
    public static void runRspServers(RemoteRobot robot, ComponentFixture rspViewTree){
        int treeElementsCount = rspViewTree.findAllText().size();

        for (int i = 0; i < treeElementsCount; i++){
            int serverNumber = i;
            rspViewTree.findAllText().get(serverNumber).click(MouseButton.RIGHT_BUTTON);
            RightClickMenu contextMenu = robot.find(RightClickMenu.class, Duration.ofSeconds(10));
            contextMenu.select("Download / Update RSP");
            final IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class);
            ideStatusBar.waitUntilAllBgTasksFinish();

            rspViewTree.findAllText().get(serverNumber).click(MouseButton.RIGHT_BUTTON);
            contextMenu = robot.find(RightClickMenu.class, Duration.ofSeconds(10));
            contextMenu.select("Start RSP");
            waitFor(Duration.ofSeconds(15), Duration.ofSeconds(1), "Server did not started.", () -> isRspServerStarted(rspViewTree ,serverNumber));

            rspViewTree.findAllText().get(serverNumber).click(MouseButton.RIGHT_BUTTON);
            contextMenu = robot.find(RightClickMenu.class, Duration.ofSeconds(10));
            contextMenu.select("Stop RSP");
            waitFor(Duration.ofSeconds(15), Duration.ofSeconds(1), "Server did not stopped.", () -> isRspServerStopped(rspViewTree ,serverNumber));
        }
    }
}
