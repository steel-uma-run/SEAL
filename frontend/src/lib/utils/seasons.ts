/**
 * Helper to determine the season (semester and year) based on a Date object.
 * Spring: Jan-Apr (months 1-4)
 * Summer: May-Aug (months 5-8)
 * Fall: Sep-Dec (months 9-12)
 */
export function getCurrentSeasonInfo(date: Date = new Date()) {
	const month = date.getMonth() + 1; // getMonth() is 0-indexed (0 = January)
	const year = date.getFullYear();

	let semester: 'SPRING' | 'SUMMER' | 'FALL';
	if (month >= 1 && month <= 4) {
		semester = 'SPRING';
	} else if (month >= 5 && month <= 8) {
		semester = 'SUMMER';
	} else {
		semester = 'FALL';
	}

	return { semester, year };
}

/**
 * Format the semester string to title case (e.g., SPRING -> Spring)
 */
export function formatSemester(semester: string): string {
	if (!semester) return "";
	return semester.charAt(0).toUpperCase() + semester.slice(1).toLowerCase();
}

/**
 * Get formatted season name (e.g., Spring 2026)
 */
export function formatSeasonName(season: { semester: string; year: number }): string {
	if (!season) return "";
	return `${formatSemester(season.semester)} ${season.year}`;
}
