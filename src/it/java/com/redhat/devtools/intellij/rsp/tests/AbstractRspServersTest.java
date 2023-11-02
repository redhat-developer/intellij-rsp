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

/**
 * @author olkornii@redhat.com
 */
public abstract class AbstractRspServersTest {
    public static boolean isRspServerStarted(ComponentFixture rspViewTree, int serverNumber){
        RemoteText server = rspViewTree.findAllText().get(serverNumber);
        return server.getText().contains("[STARTED]");
    }

    public static boolean isRspServerStopped(ComponentFixture rspViewTree, int serverNumber){
        RemoteText server = rspViewTree.findAllText().get(serverNumber);
        return server.getText().contains("[STOPPED]");
    }
}
