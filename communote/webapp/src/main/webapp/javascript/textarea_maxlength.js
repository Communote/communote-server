/**
 * Check the value of the field for the max length value.
 * <p>
 * Example to use in a textarea:<br>
 * <li>onChange="checkMaxLength(this, 200)"
 * <li>onFocus="checkMaxLength(this, 200)"
 * <li>onBlur="checkMaxLength(this, 200)"
 * <li>onKeyDown="checkMaxLength(this, 200)"
 * <li>onKeyUp="checkMaxLength(this, 200)"
 * </p>
 * 
 * @param fieldId
 *            the if of the field
 * @maxLength the max length of the field, a value less or equal 0 means no max
 *            length
 * @return the number of chars the user may input more. Returns -1 if there is
 *         no limitation
 */
function checkMaxLength(fieldId, maxLength, strLeftId) {
	if (maxLength <= 0) {
		return -1;
	}
	var field = document.getElementById(fieldId);
	// Die Variable StrLen (StringLength) steht fuer die Anzahl der eingegebenen
	// Zeichen
	var strLen = field.value.length;
	var strLeft = maxLength - strLen;
	// Wenn mehr als 130 Zeichen eingegeben werden, wird der Rest abgeschnitten;
	// der Substring extrahiert vom ersten Wert (wir zaehlen von 0 an) bis zu
	// dem letzten Wert, den er nicht mehr extrahieren kann (im Skript steht
	// 130, aber da wir bei 0 angefangen haben zu zaehlen, ist der 131. gemeint)
	if (strLen > maxLength) {
		field.value = field.value.substring(0, maxLength);
		strLeft = 0;
	}
	var strLeftField = document.getElementById(strLeftId);
	if (strLeftField != null) {
		strLeftField.innerHTML = '' + strLeft;
	}
	return strLeft;
}