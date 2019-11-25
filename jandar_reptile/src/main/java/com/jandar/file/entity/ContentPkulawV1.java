package com.jandar.file.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ContentPkulawV1 {

  private long id;
  /**
     * 案例名称
   */
  private String caseName;
  /**
   * 案由分类
   */
  private String caseType;
  /**
   * 案件字号
   */
  private String caseCode;
  /**
   * 文书类型
   */
  private String instrumentType;
  /**
   * 审结日期
   */
  private String closingDate;
  /**
   * 审理法院
   */
  private String trialCourt;
  /**
   * 审理程序
   */
  private String trialProcedure;
  /**
   * 案件情节
   */
  private String casePlot;
  /**
   * 判决结果
   */
  private String judgmentResult;
  /**
   * 判定罪名
   */
  private String judgingCrime;
  /**
   * 刑罚
   */
  private String penalty;
  /**
   * 审理法官
   */
  private String trialJudge;
  /**
   * 发布日期
   */
  private String releaseDate;
  /**
   * 案例特征
   */
  private String caseCharacteristics;
  /**
   * 裁决机构
   */
  private String adjudicationOrg;
  /**
   * 裁决日期
   */
  private String adjudicationDate;
  /**
   * 全文
   */
  private String fullText;
  /**
   * 原始url
   */
  private String sourceUrl;
  /**
   * 案件分类
   */
  private String caseDb;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getCaseName() {
    return caseName;
  }

  public void setCaseName(String caseName) {
    this.caseName = caseName;
  }


  public String getCaseType() {
    return caseType;
  }

  public void setCaseType(String caseType) {
    this.caseType = caseType;
  }


  public String getCaseCode() {
    return caseCode;
  }

  public void setCaseCode(String caseCode) {
    this.caseCode = caseCode;
  }


  public String getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(String instrumentType) {
    this.instrumentType = instrumentType;
  }


  public String getClosingDate() {
    return closingDate;
  }

  public void setClosingDate(String closingDate) {
    this.closingDate = closingDate;
  }


  public String getTrialCourt() {
    return trialCourt;
  }

  public void setTrialCourt(String trialCourt) {
    this.trialCourt = trialCourt;
  }


  public String getTrialProcedure() {
    return trialProcedure;
  }

  public void setTrialProcedure(String trialProcedure) {
    this.trialProcedure = trialProcedure;
  }


  public String getCasePlot() {
    return casePlot;
  }

  public void setCasePlot(String casePlot) {
    this.casePlot = casePlot;
  }


  public String getJudgmentResult() {
    return judgmentResult;
  }

  public void setJudgmentResult(String judgmentResult) {
    this.judgmentResult = judgmentResult;
  }


  public String getJudgingCrime() {
    return judgingCrime;
  }

  public void setJudgingCrime(String judgingCrime) {
    this.judgingCrime = judgingCrime;
  }


  public String getPenalty() {
    return penalty;
  }

  public void setPenalty(String penalty) {
    this.penalty = penalty;
  }


  public String getTrialJudge() {
    return trialJudge;
  }

  public void setTrialJudge(String trialJudge) {
    this.trialJudge = trialJudge;
  }


  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }


  public String getCaseCharacteristics() {
    return caseCharacteristics;
  }

  public void setCaseCharacteristics(String caseCharacteristics) {
    this.caseCharacteristics = caseCharacteristics;
  }


  public String getAdjudicationOrg() {
    return adjudicationOrg;
  }

  public void setAdjudicationOrg(String adjudicationOrg) {
    this.adjudicationOrg = adjudicationOrg;
  }


  public String getAdjudicationDate() {
    return adjudicationDate;
  }

  public void setAdjudicationDate(String adjudicationDate) {
    this.adjudicationDate = adjudicationDate;
  }


  public String getFullText() {
    return fullText;
  }

  public void setFullText(String fullText) {
    this.fullText = fullText;
  }


  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }


  public String getCaseDb() {
    return caseDb;
  }

  public void setCaseDb(String caseDb) {
    this.caseDb = caseDb;
  }

}
