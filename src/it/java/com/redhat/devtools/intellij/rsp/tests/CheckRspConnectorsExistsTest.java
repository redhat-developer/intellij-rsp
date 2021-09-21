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

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author olkornii@redhat.com
 */
public class CheckRspConnectorsExistsTest {
    public static void checkRspConnectors(ComponentFixture rspViewTree){
        List<RemoteText> treeElements = rspViewTree.findAllText();
        assertTrue(treeElements.size() == 2);

        String firstPosition = treeElements.get(0).getText();
        assertTrue(firstPosition.contains("Community Server Connector by Red Hat"));
        String secondPosition = treeElements.get(1).getText();
        assertTrue(secondPosition.contains("Red Hat Server Connector"));
    }
}
