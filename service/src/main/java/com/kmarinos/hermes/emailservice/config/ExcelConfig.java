package com.kmarinos.hermes.emailservice.config;

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

@Getter
public class ExcelConfig {

  private final Workbook wb;
  private CellStyle headerStyle;
  private CellStyle floatStyle;
  private CellStyle dateStyle;

  public ExcelConfig(Workbook wb){
    this.wb = wb;
    this.headerStyle = generateHeaderStyle();
    this.floatStyle = generateFloatStyle();
    this.dateStyle = generateDateStyle();
  }
  public CellStyle generateHeaderStyle(){
    CellStyle bold = wb.createCellStyle();
    Font font = wb.createFont();
    font.setBold(true);
    bold.setFont(font);
    return bold;
  }
  public CellStyle generateFloatStyle(){
    CellStyle floatFormat = wb.createCellStyle();
    floatFormat.setDataFormat((short)4);
    return floatFormat;
  }
  public CellStyle generateDateStyle(){
    CellStyle dateFormat = wb.createCellStyle();
    dateFormat.setDataFormat((short)14);
    return dateFormat;
  }
}
