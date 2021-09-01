/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.rsp.mainIdeWindow;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Bottom status bar fixture
 *
 * @author zcervink@redhat.com
 */
@DefaultXpath(by = "IdeStatusBarImpl type", xpath = "//div[@class='IdeStatusBarImpl']")
@FixtureName(name = "Ide Status Bar")
public class IdeStatusBarFixture extends CommonContainerFixture {
    public IdeStatusBarFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public ComponentFixture inlineProgressPanel() {
        return find(ContainerFixture.class, byXpath("//div[@class='InlineProgressPanel']"));
    }

    public ComponentFixture ideErrorsIcon() {
        return find(ComponentFixture.class, byXpath("//div[@class='IdeErrorsIcon']"));
    }

    public ComponentFixture withIconAndArrows(String accessiblename){
        return find(ComponentFixture.class, byXpath("//div[@accessiblename='" + accessiblename + "' and @class='WithIconAndArrows']"));
    }
}