"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.formatFormulaForPensionRevision = exports.getFormulaValueForPensionRevision = exports.getPensionRevisionRulesByConditions = exports.calculateRevisedPension = exports.formatFormulaForDate = exports.getConditionKeyForDependentEligibility = exports.calculateConditionExpressionForDependentEligibility = exports.evaluateDependentEligibilityPreCondition = exports.getDependentEligibilityForPension = exports.getDependentEligibilityForGratuity = exports.evaluateEmployeeTypePreCondition = exports.getEmployeeType = exports.evaluateNotificationPreConditions = exports.getNotifications = exports.getCalculatedBenefitValue = exports.getAdjustedValue = exports.calculateBenefitExpression = exports.getFormulaValue = exports.formatFormulaToExpression = exports.formatFormula = exports.getConditionKey = exports.calculateConditionExpression = exports.evaluatePreCondition = exports.evaluatePreConditions = exports.getBenefitFormula = exports.calculateBenefit = undefined;

var _api = require("./api");

var _envVariables = require("../envVariables");

var _envVariables2 = _interopRequireDefault(_envVariables);

var _get = require("lodash/get");

var _get2 = _interopRequireDefault(_get);

var _findIndex = require("lodash/findIndex");

var _findIndex2 = _interopRequireDefault(_findIndex);

var _isEmpty = require("lodash/isEmpty");

var _isEmpty2 = _interopRequireDefault(_isEmpty);

var _omitBy = require("lodash/omitBy");

var _omitBy2 = _interopRequireDefault(_omitBy);

var _isNil = require("lodash/isNil");

var _isNil2 = _interopRequireDefault(_isNil);

var _filter = require("lodash/filter");

var _filter2 = _interopRequireDefault(_filter);

var _calculationHelper = require("./calculationHelper");

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var math = require("mathjs");

var selectedRules = [];
var calculateBenefit = exports.calculateBenefit = function calculateBenefit(rules, employee, mdms) {
  selectedRules.length = 0;
  for (var i = 0; i < rules.benefits.length; i++) {

    getBenefitFormula(rules.benefits[i], employee, mdms);

    var benefitValue = void 0;
    var benefitFormulaExpression = "";
    switch (selectedRules[i].benefitCode) {
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_START_DATE:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_END_DATE:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_2_START_DATE:
        benefitValue = new Date(formatFormulaForDate(selectedRules[i].benefitFormula, employee, mdms));
        break;
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ONE_FORTH_HALF_YEAR_OF_SERVICE:
        benefitValue = formatFormula(selectedRules[i].benefitFormula, employee, mdms);
        benefitFormulaExpression = formatFormulaToExpression(selectedRules[i].benefitFormula, employee, mdms);
        break;
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_IR:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_DA:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION_IR:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION_DA:
        //benefitValue=formatFormula(selectedRules[i].benefitFormula ,employee,mdms);
        //benefitFormulaExpression=formatFormulaToExpression(selectedRules[i].benefitFormula ,employee,mdms);
        if (selectedRules[i].benefitFormula != null) {
          if (isNaN(Number(selectedRules[i].benefitFormula))) {
            //benefitValue=formatFormula(selectedRules[i].benefitFormula ,employee,mdms);                         
            benefitValue = Math.round(formatFormula(selectedRules[i].benefitFormula, employee, mdms));
          } else {
            benefitValue = selectedRules[i].benefitFormula;
          }
        } else {
          benefitValue = 0;
        }
        benefitFormulaExpression = formatFormulaToExpression(selectedRules[i].benefitFormula, employee, mdms);
        break;
      default:
        //benefitValue=formatFormula(selectedRules[i].benefitFormula ,employee,mdms);
        //benefitFormulaExpression=formatFormulaToExpression(selectedRules[i].benefitFormula ,employee,mdms);
        if (selectedRules[i].benefitFormula != null) {
          if (isNaN(Number(selectedRules[i].benefitFormula))) {
            //benefitValue=formatFormula(selectedRules[i].benefitFormula ,employee,mdms);                         
            benefitValue = Math.ceil(formatFormula(selectedRules[i].benefitFormula, employee, mdms));
          } else {
            benefitValue = selectedRules[i].benefitFormula;
          }
        } else {
          benefitValue = 0;
        }
        benefitFormulaExpression = formatFormulaToExpression(selectedRules[i].benefitFormula, employee, mdms);
        break;

    }

    selectedRules[i].benefitValue = benefitValue;
    selectedRules[i].finalBenefitValue = benefitValue;
    selectedRules[i].benefitFormulaExpression = benefitFormulaExpression;

    //apply adjustments
    /*
    if(selectedRules[i].adjustments && selectedRules[i].adjustments.length>0){
      for (var j = 0; j < selectedRules[i].adjustments.length; j++) { 
        let adjustment=selectedRules[i].adjustments[j];
        let adjustmentValue=formatFormula(adjustment.adjustmentFormula ,employee,mdms);
        selectedRules[i].adjustments[j].adjustmentValue=adjustmentValue;
        selectedRules[i].finalBenefitValue=getAdjustedValue(selectedRules[i].finalBenefitValue,
                                          adjustment.adjustmentType,
                                          adjustmentValue)
      }
    }
    */
  }
  for (var i = 0; i < selectedRules.length; i++) {
    switch (selectedRules[i].benefitCode) {
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_START_DATE:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_END_DATE:
      case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_2_START_DATE:
        var dateValue = new Date(selectedRules[i].finalBenefitValue);
        var day = dateValue.getDate() < 10 ? "0" + dateValue.getDate() : dateValue.getDate();
        var month = dateValue.getMonth() + 1 < 10 ? "0" + (dateValue.getMonth() + 1) : dateValue.getMonth() + 1;
        var dateValueYYYYMMDD = dateValue.getFullYear() + "-" + month + "-" + day;

        selectedRules[i].benefitValue = dateValueYYYYMMDD;
        selectedRules[i].finalBenefitValue = dateValueYYYYMMDD;

        break;
    }
  }

  return selectedRules;
};

var getBenefitFormula = exports.getBenefitFormula = function getBenefitFormula(benefit, employee, mdms) {

  var f = evaluatePreConditions(benefit.preConditions, employee, mdms);
  //conditions satisfied
  if (f != null) {
    selectedRules.push({
      benefitCode: benefit.benefitCode,
      benefitFormula: f,
      benefitValue: 0,
      finalBenefitValue: 0,
      benefitApplicable: true,
      benefitFormulaExpression: null,
      adjustments: []
    });
  } else {
    //Benefit not applicable
    selectedRules.push({
      benefitCode: benefit.benefitCode,
      benefitFormula: null,
      benefitValue: 0,
      finalBenefitValue: 0,
      benefitApplicable: false,
      benefitFormulaExpression: null,
      adjustments: []

    });
  }
};

//evaluates all root preconditions of a benfit, formual comes from the root precondition satisfies
var evaluatePreConditions = exports.evaluatePreConditions = function evaluatePreConditions(conditions, employee, mdms) {
  var formula = null;
  for (var i = 0; i < conditions.length; i++) {
    var preCondition = conditions[i];
    formula = evaluatePreCondition(preCondition, employee, mdms);
    if (formula != null) {
      break;
    }
  }
  return formula;
};

//evaluates a single precondition object
var evaluatePreCondition = exports.evaluatePreCondition = function evaluatePreCondition(condition, employee, mdms) {
  var formula = null;
  var isPreConditions = false;
  isPreConditions = calculateConditionExpression(condition.preConditions, employee, mdms);
  if (isPreConditions) {
    if (condition.formula != null) {
      formula = condition.formula;
    } else {
      var values = condition.values;
      for (var i = 0; i < values.length; i++) {
        var preCondition = values[i];
        formula = null;
        formula = evaluatePreCondition(preCondition, employee, mdms);
        if (formula != null) {
          break;
        }
      }
    }
  }

  return formula;
};

/*
//evaluate each postCondition at root level
export const evaluatePostCondition = (postCondition,  employee,mdms) => {
   

  let isPostConditions=false; 
  let adjustment;      
  isPostConditions=calculateConditionExpression(postCondition.postConditions,employee,mdms);
  if(isPostConditions){
    if(postCondition.adjustmentFormula!=null){
      adjustment={
        adjustmentType: postCondition.adjustmentType,
        adjustmentCode: postCondition.adjustmentCode,            
        adjustmentFormula: postCondition.adjustmentFormula,
        adjustmentValue: 0
      };  
    }
    else{          //postConditions under each postCondition at root level not satisfied,
      let values=postCondition.values;
      adjustment=evaluatePostConditions(values,employee,mdms);          
      
    }
  }           
  return adjustment;  
};

export const evaluatePostConditions = (postConditions,  employee,mdms) => {
   
  
  let isPostConditions=false; 
  let adjustment;      
  for (var i = 0; i < postConditions.length; i++) {      
    if(postConditions[i].postConditions!=null){
      isPostConditions=calculateConditionExpression(postConditions[i].postConditions,employee,mdms);
      if(isPostConditions){
        if(postConditions[i].adjustmentFormula!=null){
          adjustment={
            adjustmentType: postConditions[i].adjustmentType,
            adjustmentCode: postConditions[i].adjustmentCode,            
            adjustmentFormula: postConditions[i].adjustmentFormula,
            adjustmentValue: 0
          };
          
          break;
        }
        else{          
          let values=postConditions[i].values;
          evaluateConditions(values,employee,mdms);          
          
        }
      }     
    }
    
  }          
  return adjustment;  
};
*/
var calculateConditionExpression = exports.calculateConditionExpression = function calculateConditionExpression(preConditions, employee, mdms) {

  var isPreCondition = false;
  var sb = "";

  for (var i = 0; i < preConditions.length; i++) {
    var conditionKey = getConditionKey(preConditions[i].key, employee, mdms);
    var conditionOperator = preConditions[i].operator;
    var conditionValue = void 0;

    switch (preConditions[i].key) {
      case "DOR":
      case "DOD":
        conditionValue = new Date(preConditions[i].value);
        break;
      default:
        if (String(preConditions[i].key).includes("|+Y|") || String(preConditions[i].key).includes("|+M|") || String(preConditions[i].key).includes("|+D|") || String(preConditions[i].key).includes("|-Y|") || String(preConditions[i].key).includes("|-M|") || String(preConditions[i].key).includes("|-D|")) {
          conditionValue = new Date(formatFormulaForDate(preConditions[i].value, employee, mdms));
        } else {
          if (String(preConditions[i].value).includes("|")) {
            conditionValue = getConditionKey(preConditions[i].value, employee, mdms);
          } else {
            conditionValue = preConditions[i].value;
          }
        }
        break;

    }

    sb = sb + "'" + conditionKey + "' " + conditionOperator + " '" + conditionValue + "'";

    switch (conditionOperator) {
      case "==":
        if (conditionKey == conditionValue) {
          isPreCondition = true;
        }
        break;
      case "<":
        if (conditionKey < conditionValue) {
          isPreCondition = true;
        }
        break;
      case "<=":
        if (conditionKey <= conditionValue) {
          isPreCondition = true;
        }
        break;
      case ">":
        if (conditionKey > conditionValue) {
          isPreCondition = true;
        }
        break;
      case ">=":
        if (conditionKey >= conditionValue) {
          isPreCondition = true;
        }
        break;
      case "!=":
        if (conditionKey != conditionValue) {
          isPreCondition = true;
        }
        break;
    }

    //any of the preConditions satisfied then preConditions also satisfied
    if (isPreCondition) {
      break;
    }
  }
  return isPreCondition;
};

var getConditionKey = exports.getConditionKey = function getConditionKey(key, employee, mdms) {
  var keyValue = key;
  switch (key) {
    case "YEAR_OF_SERVICE":
      keyValue = (0, _calculationHelper.getYearOfService)(employee);
      break;
    case "REASON_FOR_RETIREMENT":
      keyValue = String(employee.reasonForRetirement).toUpperCase();
      break;
    case "IS_OPTED_FOR_COMMUTATION":
      keyValue = String(employee.isCommutationOpted).toUpperCase();
      break;
    case "DOR":
      keyValue = new Date(employee.dateOfRetirement);
      break;
    case "IS_CONVICTED_SERIOUS_CRIME_OR_GUILTY_GRAVE_MISCONDUCT":
      keyValue = String(employee.isConvictedSeriousCrimeOrGraveMisconduct).toUpperCase();
      break;
    case "IS_JUDICIAL_PROCEEDING_CONTINUED":
      keyValue = String(employee.isAnyJudicialProceedingIsContinuing).toUpperCase();
      break;
    case "IF_MISCONDUCT_INSOLVENCY_INEFFICIENCY":
      keyValue = String(employee.isAnyMisconductInsolvencyInefficiency).toUpperCase();
      break;
    case "IS_TAKEN_GRATUITY_COMMUTATION_TERMINAL_BENEFIT":
      keyValue = String(employee.isTakenGratuityCommutationTerminalBenefit).toUpperCase();
      break;
    case "IS_TAKEN_MONTHLY_PENSION_AND_GRATUITY":
      keyValue = String(employee.isTakenMonthlyPensionAndGratuity).toUpperCase();
      break;
    case "IS_TAKEN_COMPENSATION_PENSION_AND_GRATUITY":
      keyValue = String(employee.isTakenCompensationPensionAndGratuity).toUpperCase();
      break;
    case "IS_DUES_PRESENT":
      keyValue = String(employee.isDuesPresent).toUpperCase();
      break;
    case "IS_DUES_AMOUNT_DECIDED":
      keyValue = String(employee.isDuesAmountDecided).toUpperCase();
      break;
    case "DUES_AMOUNT":
      keyValue = Number(employee.dues);
      break;
    case "|DCRG|":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_DCRG);
      break;
    case "TRUE":
      keyValue = "TRUE";
      break;
    case "FALSE":
      keyValue = "FALSE";
      break;
    case "EMPLOYEE_TYPE":
      keyValue = String(employee.employeeType).toUpperCase();
      break;
    case "|DAILY_WAGER_10_YEAR_PERMANENT_DATE| - |DOJ|":
      var configurationValue = new Date(getFormulaValue("|DAILY_WAGER_10_YEAR_PERMANENT_DATE|", null, employee, mdms));
      var doj = new Date(getFormulaValue("|DOJ|", null, employee, mdms));
      var monthsDaysInRange = (0, _calculationHelper.getMonthsDaysInRange)(doj, configurationValue);
      var months = Number(monthsDaysInRange.split("|")[0]);
      keyValue = Math.trunc(months / 12);
      break;
    case "IF_INJURED_DIES_BY_ATTACK_OF_EXTREMISTS_DECOITS_SMUGGLERS_ANTI_SOCIAL":
      keyValue = String(employee.diesInExtremistsDacoitsSmugglerAntisocialAttack).toUpperCase();
      break;
    case "LPD":
      keyValue = Number(employee.lpd);
      break;
    case "DOD":
      keyValue = new Date(employee.dateOfDeath);
      break;
    case "PROVISIONAL_PENSION":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PROVISIONAL_PENSION);
      break;
    case "COMPASSIONATE_PENSION":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMPASSIONATE_PENSION);
      break;
    case "COMPENSATION_PENSION":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMPENSATION_PENSION);
      break;
    case "IS_COMPASSIONATE_PENSION_GRANTED":
      keyValue = String(employee.isCompassionatePensionGranted).toUpperCase();
      break;
    case "DOB":
      keyValue = new Date(employee.dob);
      break;
    case "FAMILY_PENSION_1_END_DATE":
      keyValue = new Date(getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_END_DATE));
      break;
    case "EMPLOYEE_GROUP":
      keyValue = String(employee.employeeGroup).toUpperCase();
      break;
    case "DISABILITY_PERCENTAGE":
      keyValue = Number(employee.employeeDisability.disabilityPercentage);
      break;
    case "INVALID_PENSION":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_INVALID_PENSION);
      break;
    case "WOUND_EXTRAORDINARY_PENSION":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_WOUND_EXTRAORDINARY_PENSION);
      break;
    case "ATTENDANT_ALLOWANCE":
      keyValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ATTENDANT_ALLOWANCE);
      break;
    case "IF_ATTENDANT_ALLOWANCE_GRANTED":
      keyValue = String(employee.employeeDisability.attendantAllowanceGranted).toUpperCase();
      break;
    case "IS_ACCIDENTAL_DEATH":
      keyValue = String(employee.isEmployeeDiesInAccidentalDeath).toUpperCase();
      break;
    case "IS_INJURED_DIED_IN_TERRORIST_ATTACK":
      keyValue = String(employee.isEmployeeDiesInTerroristAttack).toUpperCase();
      break;
    case "FAMILY_PENSION_2_START_DATE":
      keyValue = new Date(getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_2_START_DATE));
      break;
    default:
      if (String(key).includes("|+Y|") || String(key).includes("|+M|") || String(key).includes("|+D|") || String(key).includes("|-Y|") || String(key).includes("|-M|") || String(key).includes("|-D|")) {
        keyValue = new Date(formatFormulaForDate(key, employee, mdms));
      } else {
        keyValue = formatFormula(key, employee, mdms);
      }
      break;

  }

  return keyValue;
};

var formatFormula = exports.formatFormula = function formatFormula(expression, employee, mdms) {
  var expressionValue = 0;
  if (expression) {
    expression = String(expression).replace(/\s/g, '');
    var strParameters = String(expression).replace(' ', '').split(new RegExp('[-+()*/%^]', 'g'));
    for (var i = 0; i < strParameters.length; i++) {
      if (strParameters[i] != "") {
        expression = String(expression).replace(strParameters[i], getFormulaValue(strParameters[i], expression, employee, mdms));
      }
    }

    expressionValue = calculateBenefitExpression(expression);
  }

  return expressionValue;
};

var formatFormulaToExpression = exports.formatFormulaToExpression = function formatFormulaToExpression(expression, employee, mdms) {

  if (expression) {
    expression = String(expression).replace(/\s/g, '');
    var strParameters = String(expression).replace(' ', '').split(new RegExp('[-+()*/%^]', 'g'));
    for (var i = 0; i < strParameters.length; i++) {
      if (strParameters[i] != "") {
        expression = String(expression).replace(strParameters[i], getFormulaValue(strParameters[i], expression, employee, mdms));
      }
    }
  }

  return expression;
};

var getFormulaValue = exports.getFormulaValue = function getFormulaValue(parameter, expression, employee, mdms) {
  var parameterValue = parameter;
  switch (parameter) {
    case "|YEAR_OF_SERVICE|":
      parameterValue = (0, _calculationHelper.getYearOfService)(employee);
      break;
    case "|LPD|":
      parameterValue = Number(employee.lpd);
      break;
    case "|IR|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_IR);
      break;
    case "|COMMUTATION_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMMUTATION_PENSION);
      break;
    case "|BASIC_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_BASIC_PENSION);
      break;
    case "|PROVISIONAL_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PROVISIONAL_PENSION);
      break;
    case "|PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION);
      break;
    case "|PENSION_DEDUCTION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION_DEDUCTION);
      break;
    case "|COMMUTATION_VALUE|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMMUTATION_VALUE);
      break;
    case "|COMMUTED_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMMUTED_PENSION);
      break;
    case "|DA|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_DA);
      break;
    case "|PENSION_DA|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION_DA);
      break;
    case "|DCRG|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_DCRG);
      break;
    case "|ADDITIONAL_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ADDITIONAL_PENSION);
      break;
    case "|COMPASSIONATE_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMPASSIONATE_PENSION);
      break;
    case "|COMPENSATION_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_COMPENSATION_PENSION);
      break;
    case "|NET_DEDUCTION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_NET_DEDUCTION);
      break;
    case "|TOTAL_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_TOTAL_PENSION);
      break;
    case "|FINAL_CALCULATED_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FINAL_CALCULATED_PENSION);
      break;
    case "|DCRG_DUES_DEDUCTION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_DCRG_DUES_DEDUCTION);
      break;
    case "|COMMUTATION_PERCENTAGE|":
      parameterValue = (0, _calculationHelper.getCommutationPercentage)(employee, mdms);
      break;
    case "|COMMUTATION_MULTIPLIER|":
      parameterValue = (0, _calculationHelper.getCommutationMultiplier)(employee, mdms);
      break;
    case "|DA_PERCENTAGE|":
      parameterValue = (0, _calculationHelper.getDAPercentage)(employee, mdms);
      break;
    case "|IR_PERCENTAGE|":
      parameterValue = (0, _calculationHelper.getIRPercentage)(employee, mdms);
      break;
    case "|ADDITIONAL_PENSION_PERCENTAGE|":
      parameterValue = (0, _calculationHelper.getAdditionalPensionPercentage)(employee, mdms);
      break;
    case "|DAILY_WAGER_10_YEAR_PERMANENT_DATE|":
      parameterValue = (0, _calculationHelper.getPensionConfigurationValue)("DAILY_WAGER_10_YEAR_PERMANENT_DATE", mdms);
      break;
    case "|DOJ|":
      parameterValue = (0, _calculationHelper.getDOJ)(employee);
      break;
    case "|DOD|":
      parameterValue = new Date(employee.dateOfDeath);
      break;
    case "|OVERPAYMENT|":
      parameterValue = Number(employee.overPayment);
      break;
    case "|IT|":
      parameterValue = Number(employee.incomeTax);
      break;
    case "|CESS|":
      parameterValue = Number(employee.cess);
      break;
    case "|MR|":
      parameterValue = Number(employee.medicalRelief);
      break;
    case "|FMA|":
      parameterValue = Number(employee.fma);
      break;
    case "|MISC|":
      parameterValue = Number(employee.miscellaneous);
      break;
    case "|FAMILY_PENSION_1|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1);
      break;
    case "|ADDITIONAL_FAMILY_PENSION_1|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ADDITIONAL_FAMILY_PENSION_1);
      break;
    case "|ADDITIONAL_PENSION_PERCENTAGE_FOR_FAMILY|":
      parameterValue = (0, _calculationHelper.getAdditionalPensionPercentageForFamily)(employee.dependents, mdms);
      break;
    case "|DOR|":
      parameterValue = new Date(employee.dateOfRetirement);
      break;
    case "|HALF_YEAR_OF_SERVICE|":
      parameterValue = (0, _calculationHelper.getHalfYearOfService)(employee);
      break;
    case "|PENSIONER_FAMILY_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSIONER_FAMILY_PENSION);
      break;
    case "|PENSION_IR|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_PENSION_IR);
      break;
    case "|ONE_FORTH_HALF_YEAR_OF_SERVICE|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ONE_FORTH_HALF_YEAR_OF_SERVICE);
      break;
    case "|INVALID_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_INVALID_PENSION);
      break;
    case "|WOUND_EXTRAORDINARY_PENSION|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_WOUND_EXTRAORDINARY_PENSION);
      break;
    case "|APPROVED_WOUND_EXTRAORDINARY_PENSION|":
      parameterValue = Number(employee.employeeDisability.woundExtraordinaryPension);
      break;
    case "|ATTENDANT_ALLOWANCE|":
      parameterValue = getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_ATTENDANT_ALLOWANCE);
      break;
    case "|DISABILITY_PERCENTAGE|":
      parameterValue = Number(employee.employeeDisability.disabilityPercentage);
      break;
    case "|DUES_AMOUNT|":
      parameterValue = Number(employee.dues);
      break;
    case "|FAMILY_PENSION_2_START_DATE|":
      parameterValue = new Date(getCalculatedBenefitValue(_envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_2_START_DATE));
      break;

  }

  return parameterValue;
};

var calculateBenefitExpression = exports.calculateBenefitExpression = function calculateBenefitExpression(expression) {
  var result = 0;
  //result=Math.ceil(Number(math.evaluate(expression)));
  result = Number(math.evaluate(expression));
  return result;
};

var getAdjustedValue = exports.getAdjustedValue = function getAdjustedValue(benefitValue, adjustmentType, adjustedValue) {
  var finalBenefitValue = Number(benefitValue);

  switch (String(adjustmentType).toUpperCase()) {
    case "DEDUCTION":
      finalBenefitValue = Number(finalBenefitValue) - Number(adjustedValue);
      break;
    case "ADDITION":
      finalBenefitValue = Number(finalBenefitValue) + Number(adjustedValue);
      break;

  }

  return finalBenefitValue;
};

var getCalculatedBenefitValue = exports.getCalculatedBenefitValue = function getCalculatedBenefitValue(benefitCode) {
  var benefitValue = void 0;
  var benefit = (0, _filter2.default)(selectedRules, function (x) {
    return x.benefitCode == benefitCode;
  });
  if (benefit && benefit.length > 0) {
    benefitValue = benefit[0].finalBenefitValue;
  }
  switch (benefitCode) {
    case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_START_DATE:
    case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_1_END_DATE:
    case _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_FAMILY_PENSION_2_START_DATE:
      benefitValue = benefitValue;
      break;
    default:
      benefitValue = Number(benefitValue);
      break;

  }

  return benefitValue;
};

var getNotifications = exports.getNotifications = function getNotifications(employee, mdms) {
  var selectedNotifications = [];
  var notifications = (0, _get2.default)(mdms, "MdmsRes.pension.notifications");
  //const notifications=get(mdms,"notifications");

  for (var i = 0; i < notifications.length; i++) {
    var text = null;
    text = evaluateNotificationPreConditions(notifications[i].preConditions, employee, mdms);

    //conditions satisfied
    if (text != null) {
      var notification = {
        notificationCode: notifications[i].notificationCode,
        notificationText: text
      };

      selectedNotifications.push(notification);
    }
  }

  return selectedNotifications;
};

var notificationText = null;
var evaluateNotificationPreConditions = exports.evaluateNotificationPreConditions = function evaluateNotificationPreConditions(conditions, employee, mdms) {

  var isPreConditions = false;
  for (var i = 0; i < conditions.length; i++) {
    if (conditions[i].preConditions != null) {
      isPreConditions = calculateConditionExpression(conditions[i].preConditions, employee, mdms);
      if (isPreConditions) {
        if (conditions[i].notificationText != null) {
          notificationText = conditions[i].notificationText;
          break;
        } else {
          notificationText = null;
          var values = conditions[i].values;
          evaluateNotificationPreConditions(values, employee, mdms);
        }
      } else {
        notificationText = null;
      }
    }
  }
  return notificationText;
};

var getEmployeeType = exports.getEmployeeType = function getEmployeeType(employee, mdms) {
  var mdmsEmployeeType = (0, _get2.default)(mdms, "MdmsRes.pension.employeeType");
  //const mdmsEmployeeType=get(mdms,"employeeType"); 
  var employeeType = null;
  for (var i = 0; i < mdmsEmployeeType[0].preConditions.length; i++) {
    var preCondition = mdmsEmployeeType[0].preConditions[i];
    employeeType = evaluateEmployeeTypePreCondition(preCondition, employee, mdms);
    if (employeeType != null) {
      break;
    }
  }

  return employeeType;
};

var evaluateEmployeeTypePreCondition = exports.evaluateEmployeeTypePreCondition = function evaluateEmployeeTypePreCondition(condition, employee, mdms) {
  var employeeType = null;
  var isPreConditions = false;
  isPreConditions = calculateConditionExpression(condition.preConditions, employee, mdms);
  if (isPreConditions) {
    if (condition.employeeType != null) {
      employeeType = condition.employeeType;
    } else {
      var values = condition.values;
      for (var i = 0; i < values.length; i++) {
        var preCondition = values[i];
        employeeType = null;
        employeeType = evaluateEmployeeTypePreCondition(preCondition, employee, mdms);
        if (employeeType != null) {
          break;
        }
      }
    }
  }

  return employeeType;
};

var getDependentEligibilityForGratuity = exports.getDependentEligibilityForGratuity = function getDependentEligibilityForGratuity(dependent, mdms) {
  var mdmsEligibility = (0, _get2.default)(mdms, "MdmsRes.pension.dependentsEligibilityForGratuity");
  //const mdmsEligibility=get(mdms,"dependentsEligibilityForGratuity");
  var eligibility = null;
  for (var i = 0; i < mdmsEligibility[0].preConditions.length; i++) {
    var preCondition = mdmsEligibility[0].preConditions[i];
    eligibility = evaluateDependentEligibilityPreCondition(preCondition, dependent);
    if (eligibility != null) {
      break;
    }
  }

  return eligibility;
};

var getDependentEligibilityForPension = exports.getDependentEligibilityForPension = function getDependentEligibilityForPension(dependent, mdms, dependents) {
  var mdmsEligibility = (0, _get2.default)(mdms, "MdmsRes.pension.dependentsEligibilityForPension");
  //const mdmsEligibility=get(mdms,"dependentsEligibilityForPension");
  var eligibility = null;
  for (var i = 0; i < mdmsEligibility[0].preConditions.length; i++) {
    var preCondition = mdmsEligibility[0].preConditions[i];
    eligibility = evaluateDependentEligibilityPreCondition(preCondition, dependent, dependents);
    if (eligibility != null) {
      break;
    }
  }

  return eligibility;
};

var evaluateDependentEligibilityPreCondition = exports.evaluateDependentEligibilityPreCondition = function evaluateDependentEligibilityPreCondition(condition, dependent, dependents) {
  var eligibility = null;
  var isPreConditions = false;
  isPreConditions = calculateConditionExpressionForDependentEligibility(condition.preConditions, dependent, dependents);
  if (isPreConditions) {
    if (condition.eligibility != null) {
      eligibility = condition.eligibility;
    } else {
      var values = condition.values;
      for (var i = 0; i < values.length; i++) {
        var preCondition = values[i];
        eligibility = null;
        eligibility = evaluateDependentEligibilityPreCondition(preCondition, dependent, dependents);
        if (eligibility != null) {
          break;
        }
      }
    }
  }

  return eligibility;
};

var calculateConditionExpressionForDependentEligibility = exports.calculateConditionExpressionForDependentEligibility = function calculateConditionExpressionForDependentEligibility(preConditions, dependent, dependents) {

  var isPreCondition = false;

  for (var i = 0; i < preConditions.length; i++) {
    var conditionKey = getConditionKeyForDependentEligibility(preConditions[i].key, dependent, dependents);
    var conditionOperator = preConditions[i].operator;
    var conditionValue = preConditions[i].value;

    switch (conditionOperator) {
      case "==":
        if (conditionKey == conditionValue) {
          isPreCondition = true;
        }
        break;
      case "<":
        if (conditionKey < conditionValue) {
          isPreCondition = true;
        }
        break;
      case "<=":
        if (conditionKey <= conditionValue) {
          isPreCondition = true;
        }
        break;
      case ">":
        if (conditionKey > conditionValue) {
          isPreCondition = true;
        }
        break;
      case ">=":
        if (conditionKey >= conditionValue) {
          isPreCondition = true;
        }
        break;
      case "!=":
        if (conditionKey != conditionValue) {
          isPreCondition = true;
        }
        break;
    }

    //any of the preConditions satisfied then preConditions also satisfied
    if (isPreCondition) {
      break;
    }
  }
  return isPreCondition;
};

var getConditionKeyForDependentEligibility = exports.getConditionKeyForDependentEligibility = function getConditionKeyForDependentEligibility(key, dependent, dependents) {
  var keyValue = "";
  switch (key) {
    case "RELATION":
    case "ELIGIBLE_RELATION":
      keyValue = String(dependent.relationship).toUpperCase();
      break;
    case "IS_DISABLED":
      keyValue = String(dependent.isDisabled).toUpperCase();
      break;
    case "AGE":
      keyValue = (0, _calculationHelper.getAge)(dependent.dob);
      break;
    case "IS_ELDEST":
      keyValue = (0, _calculationHelper.isEldestDependent)(dependent, dependents) == true ? "TRUE" : "FALSE";
      break;
  }

  return keyValue;
};

var formatFormulaForDate = exports.formatFormulaForDate = function formatFormulaForDate(expression, employee, mdms) {
  var expressionValue = void 0;
  if (expression) {
    expression = String(expression).replace(/\s/g, '');
    var strParameters = String(expression).split(new RegExp('[|]', 'g'));
    var dateValue = new Date(getConditionKey(strParameters[1], employee, mdms));
    var op = strParameters[3];
    var value = Number(strParameters[5]);
    var value2 = 0;
    if (strParameters.length > 7) {
      value2 = Number(strParameters[7]);
    }
    switch (op) {
      case "+D":
        expressionValue = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate() + value, dateValue.getHours(), dateValue.getMinutes());
        break;
      case "+M":
        expressionValue = new Date(dateValue.getFullYear(), dateValue.getMonth() + value, dateValue.getDate(), dateValue.getHours(), dateValue.getMinutes());
        break;
      case "+Y":
        expressionValue = new Date(dateValue.getFullYear() + value, dateValue.getMonth(), dateValue.getDate(), dateValue.getHours(), dateValue.getMinutes());
        break;
      case "-D":
        expressionValue = new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate() - value, dateValue.getHours(), dateValue.getMinutes());
        break;
      case "-M":
        expressionValue = new Date(dateValue.getFullYear(), dateValue.getMonth() - value, dateValue.getDate(), dateValue.getHours(), dateValue.getMinutes());
        break;
      case "-Y":
        expressionValue = new Date(dateValue.getFullYear() - value, dateValue.getMonth(), dateValue.getDate(), dateValue.getHours(), dateValue.getMinutes());
        break;
      case "+Y-D":
        expressionValue = new Date(dateValue.getFullYear() + value, dateValue.getMonth(), dateValue.getDate() - value2, dateValue.getHours(), dateValue.getMinutes());
        break;
      default:
        expressionValue = dateValue;
        break;

    }
  }

  return expressionValue;
};

//Calculate Revised Pension
var calculateRevisedPension = exports.calculateRevisedPension = function calculateRevisedPension(rules, benefits) {

  var selectedRules = getPensionRevisionRulesByConditions(rules);

  for (var i = 0; i < selectedRules.length; i++) {
    var benefitValue = formatFormulaForPensionRevision(selectedRules[i].benefitFormula, benefits, selectedRules);
    selectedRules[i].benefitValue = benefitValue;
    selectedRules[i].finalBenefitValue = benefitValue;
    /*
    switch(String(selectedRules[i].benefitCode).toUpperCase()){ 
      case "TOTAL_PENSION":
          benefits.totalPension=benefitValue;                  
          break; 
        case "NET_DEDUCTION":
          benefits.netDeductions=benefitValue;                  
          break; 
        case "FINAL_CALCULATED_PENSION":
          benefits.finalCalculatedPension=benefitValue;                    
          break; 
    } 
    */
  }

  return selectedRules;
};

var getPensionRevisionRulesByConditions = exports.getPensionRevisionRulesByConditions = function getPensionRevisionRulesByConditions(rules) {

  var benefits = [];

  for (var i = 0; i < rules.length; i++) {

    var benefitFormula = evaluatePreConditions(rules[i].preConditions);
    //conditions satisfied
    if (benefitFormula != null) {
      var benefit = {
        benefitCode: rules[i].benefitCode,
        benefitFormula: benefitFormula,
        benefitValue: 0,
        finalBenefitValue: 0
      };
      benefits.push(benefit);
    }
  }

  return benefits;
};

var getFormulaValueForPensionRevision = exports.getFormulaValueForPensionRevision = function getFormulaValueForPensionRevision(parameter, benefits, selectedRules) {
  var parameterValue = parameter;
  switch (parameter) {
    case "|BASIC_PENSION|":
      parameterValue = Number(benefits.basicPension);
      break;
    case "|PENSION_DA|":
      parameterValue = Number(benefits.da);
      break;
    case "|COMMUTED_PENSION|":
      parameterValue = Number(benefits.commutedPension);
      break;
    case "|ADDITIONAL_PENSION|":
      parameterValue = Number(benefits.additionalPension);
      break;
    case "|PENSION_IR|":
      parameterValue = Number(benefits.interimRelief);
      break;
    case "|MR|":
      parameterValue = Number(benefits.medicalRelief);
      break;
    case "|FMA|":
      parameterValue = Number(benefits.fma);
      break;
    case "|MISC|":
      parameterValue = Number(benefits.miscellaneous);
      break;
    case "|PENSION_DEDUCTION|":
      parameterValue = Number(benefits.pensionDeductions);
      break;
    case "|OVERPAYMENT|":
      parameterValue = Number(benefits.overPayment);
      break;
    case "|IT|":
      parameterValue = Number(benefits.incomeTax);
      break;
    case "|CESS|":
      parameterValue = Number(benefits.cess);
      break;
    case "|TOTAL_PENSION|":
      var totalPension = (0, _filter2.default)(selectedRules, function (x) {
        return x.benefitCode == _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_TOTAL_PENSION;
      });
      parameterValue = Number(totalPension[0].finalBenefitValue);
      break;
    case "|NET_DEDUCTION|":
      var netDeductions = (0, _filter2.default)(selectedRules, function (x) {
        return x.benefitCode == _envVariables2.default.EGOV_PENSION_BENEFIT_CODE_NET_DEDUCTION;
      });
      parameterValue = Number(netDeductions[0].finalBenefitValue);
      break;
    case "|WOUND_EXTRAORDINARY_PENSION|":
      parameterValue = benefits.woundExtraordinaryPension ? Number(benefits.woundExtraordinaryPension) : 0;
      break;
    case "|ATTENDANT_ALLOWANCE|":
      parameterValue = benefits.attendantAllowance ? Number(benefits.attendantAllowance) : 0;
      break;

  }

  return parameterValue;
};

var formatFormulaForPensionRevision = exports.formatFormulaForPensionRevision = function formatFormulaForPensionRevision(expression, benefits, selectedRules) {
  var expressionValue = 0;
  if (expression) {
    expression = String(expression).replace(/\s/g, '');
    var strParameters = String(expression).replace(' ', '').split(new RegExp('[-+()*/%^]', 'g'));
    for (var i = 0; i < strParameters.length; i++) {
      if (strParameters[i] != "") {
        expression = String(expression).replace(strParameters[i], getFormulaValueForPensionRevision(strParameters[i], benefits, selectedRules));
      }
    }

    expressionValue = calculateBenefitExpression(expression);
  }

  return expressionValue;
};
//# sourceMappingURL=calculationManager.js.map