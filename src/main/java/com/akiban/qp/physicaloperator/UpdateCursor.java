/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.qp.physicaloperator;

import com.akiban.qp.row.Row;

public final class UpdateCursor extends ChainedCursor {

    private final UpdateLambda updateLambda;

    public UpdateCursor(Cursor input, UpdateLambda updateLambda) {
        super(input);
        this.updateLambda = updateLambda;
    }

    // Cursor interface

    @Override
    public boolean next() {
        if (input.next()) {
            Row row = this.input.currentRow();
            if (!updateLambda.rowIsApplicable(row)) {
                return true;
            }
            Row currentRow = updateLambda.applyUpdate(row);
            input.updateCurrentRow(currentRow);
            return true;
        }
        return false;
    }
}
