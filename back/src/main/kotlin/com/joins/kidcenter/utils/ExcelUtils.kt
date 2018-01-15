/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joins.kidcenter.utils

import com.github.excelmapper.core.engine.CellCoordinate
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFShape

object ExcelUtils {

    val poiColWidthFactor = 256
    val defaultCharWidthPx = 7
    val ptToPxFactor = 0.75

    fun getCell(sheet: Sheet, cellCoordinate: CellCoordinate, createIfNotExist: Boolean): Cell? {
        val rowNum = cellCoordinate.row
        val colNum = cellCoordinate.column
        var row: Row? = sheet.getRow(rowNum)
        if (row == null) {
            if (createIfNotExist) {
                row = sheet.createRow(rowNum)
            } else {
                return null
            }
        }
        var cell: Cell? = row!!.getCell(colNum)
        if (cell == null) {
            cell = if (createIfNotExist) row.createCell(colNum) else null
        }
        return cell
    }

    fun setCellSizeInPx(sheet: Sheet, cellCoordinate: CellCoordinate, width: Int, height: Int) {
        setColumnWidthInPx(cellCoordinate.column, sheet, width)
        setRowHeightInPx(cellCoordinate, height, sheet)
    }

    fun setColumnWidthInPx(columnIndex: Int, sheet: Sheet, width: Int) {
        sheet.setColumnWidth(columnIndex, width * poiColWidthFactor / defaultCharWidthPx)
    }

    fun setRowHeightInPx(cellCoordinate: CellCoordinate, height: Int, sheet: Sheet) {
        val rowNum = cellCoordinate.row
        var row: Row? = sheet.getRow(rowNum)
        if (row == null) {
            row = sheet.createRow(rowNum)
        }
        row!!.heightInPoints = (height * ptToPxFactor).toFloat()
    }

    fun addPicture(sheet: Sheet, pictureBytes: ByteArray, pictureType: Int): Int {
        val wb = sheet.workbook
        return wb.addPicture(pictureBytes, pictureType)
    }

    fun insertPicture(sheet: Sheet, cellCoordinate: CellCoordinate, pictureIdx: Int, width: Int, height: Int) {
        val wb = sheet.workbook
        val helper = wb.creationHelper
        val drawing = sheet.createDrawingPatriarch()
        val anchor = helper.createClientAnchor()
        anchor.setCol1(cellCoordinate.column)
        anchor.row1 = cellCoordinate.row
        anchor.setCol2(cellCoordinate.column)
        anchor.row2 = cellCoordinate.row
        anchor.dx1 = 0
        anchor.dy1 = 0
        anchor.dx2 = XSSFShape.EMU_PER_PIXEL * width
        anchor.dy2 = XSSFShape.EMU_PER_PIXEL * height

        drawing.createPicture(anchor, pictureIdx)
    }

}