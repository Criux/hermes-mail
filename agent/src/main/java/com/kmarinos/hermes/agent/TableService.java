package com.kmarinos.hermes.agent;

import com.kmarinos.hermes.agent.config.ExcelConfig;
import com.kmarinos.hermes.domain.email.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TableService {

  public void addToExcel(Table table, Workbook wb) {
    log.info("Adding table with {} rows.",table.getRows().size());
    XSSFSheet sheet;
    ExcelConfig excelConfig = new ExcelConfig(wb);
    if (table.getName() != null && !table.getName().isEmpty()) {
      sheet = (XSSFSheet) wb.createSheet(WorkbookUtil.createSafeSheetName(table.getName(), '_'));
    } else {
      sheet = (XSSFSheet) wb.createSheet();
    }
    int currentRow = 0;
    // check if header names are set and add them
    if (table.getHeaders() != null
        && (table.getHeaders().stream()
                .filter(h -> h.getName() != null && !h.getName().isEmpty())
                .count()
            > 0)) {
      currentRow = 1;
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < table.getHeaders().size(); i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(table.getHeaders().get(i).getName());
        if (excelConfig.getHeaderStyle() != null) {
          cell.setCellStyle(excelConfig.getHeaderStyle());
        }
      }
    }
    if (table.getRows() == null) {
      return;
    }
    for (int i = 0; i < table.getRows().size(); i++) {
      Row row = sheet.createRow(currentRow+i);
      for (int j = 0; j < table.getRows().get(i).getValues().size(); j++) {
        try {
          Object cellValue = table.getRows().get(i).getValues().get(j);
          Class cellClass = Class.forName(table.getHeaders().get(j).getColumnType());
          if (cellValue != null) {
            cellClass = cellValue.getClass();
          }
          addCellToRow(row, j, cellValue, cellClass, excelConfig);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
    // auto size all the columns
    for (int i = 0; i < table.getHeaders().size(); i++) {
      int oldWidth = sheet.getColumnWidth(i);
      sheet.autoSizeColumn(i);
      if (oldWidth > sheet.getColumnWidth(i)) {
        sheet.setColumnWidth(i, oldWidth);
      }
    }
    if (table.getHeaders().size() > 0) {
      sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, table.getHeaders().size() - 1));
      sheet.addIgnoredErrors(
          new CellRangeAddress(0, table.getRows().size(), 0, table.getHeaders().size() - 1),
          IgnoredErrorType.NUMBER_STORED_AS_TEXT);
    }
  }

  private <T> void addCellToRow(
      Row row, int pos, Object value, Class<T> clazz, ExcelConfig excelConfig) {
    if(clazz.equals(String.class)){
      addCellToRow(row,pos,(String)value,null);
    }else if(clazz.equals(Integer.class)){
      addCellToRow(row,pos,(Integer)value,null);
    }else if(clazz.equals(BigDecimal.class)){
      addCellToRow(row,pos,(BigDecimal)value,excelConfig.getFloatStyle());
    }else if(clazz.equals(LocalDate.class)){
      addCellToRow(row,pos,(LocalDate)value,excelConfig.getDateStyle());
    }else if(clazz.equals(LocalDateTime.class)){
      addCellToRow(row,pos,(LocalDateTime)value,excelConfig.getDateStyle());
    }else if(clazz.equals(java.sql.Date.class)){
      addCellToRow(row,pos,(java.sql.Date)value,excelConfig.getDateStyle());
    }else if(clazz.equals(Timestamp.class)){
      addCellToRow(row,pos,(Timestamp)value,excelConfig.getDateStyle());
    }else{
      addCellToRow(row,pos,"CANT PARSE:"+clazz.getName(),null);
    }
  }
  private void addCellToRow(Row row, int pos,String value, CellStyle style){
    Cell cell = row.createCell(pos);
    cell.setCellValue(value);
    if(style !=null){
      cell.setCellStyle(style);
    }
  }
  private void addCellToRow(Row row, int pos,Integer value, CellStyle style){
    Cell cell = row.createCell(pos);
    if(value == null){
      addCellToRow(row,pos,"",style);
    }else{
      cell.setCellValue(value);
      if(style !=null){
        cell.setCellStyle(style);
      }
    }
  }
  private void addCellToRow(Row row, int pos,LocalDate value, CellStyle style){
    Cell cell = row.createCell(pos);
    cell.setCellValue(value);
    if(style !=null){
      cell.setCellStyle(style);
    }
  }
  private void addCellToRow(Row row, int pos,BigDecimal value, CellStyle style){
    Cell cell = row.createCell(pos);
    cell.setCellValue(value.floatValue());
    if(style !=null){
      cell.setCellStyle(style);
    }
  }
  private void addCellToRow(Row row, int pos,LocalDateTime value, CellStyle style){
    Cell cell = row.createCell(pos);
    cell.setCellValue(value);
    if(style !=null){
      cell.setCellStyle(style);
    }
  }
  private void addCellToRow(Row row, int pos,java.sql.Date value, CellStyle style){
    if(value == null){
      addCellToRow(row,pos,"",style);
    }else{
      addCellToRow(row,pos,value.toLocalDate(),style);
    }
  }
  private void addCellToRow(Row row, int pos, Timestamp value, CellStyle style){
    if(value == null){
      addCellToRow(row,pos,"",style);
    }else{
      addCellToRow(row,pos,value.toLocalDateTime(),style);
    }
  }
}
