/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.rsp.ui.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Simple util to run stuff in UI thread
 */
public class UIHelper {
    public static void executeInUI(Runnable runnable) {
        executeInUISync(runnable);
    }

    public static void executeInUISync(Runnable runnable) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            runnable.run();
        } else {
            ApplicationManager.getApplication().invokeAndWait(runnable);
        }
    }

    public static void executeInUIAsync(Runnable runnable, ModalityState modality) {
        ApplicationManager.getApplication().invokeLater(runnable, modality);
    }
    public static void executeInUIAsync(Runnable runnable, Component componentForModality) {
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.stateForComponent(componentForModality));
    }


    public static <T> T executeInUI(Supplier<T> supplier) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            return supplier.get();
        } else {
            final Object[] val = new Object[1];
            ApplicationManager.getApplication().invokeAndWait(() -> val[0] = supplier.get());
            return (T) val[0];
        }
    }
}
