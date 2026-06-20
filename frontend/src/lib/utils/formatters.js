/**
 * Format date for Master view (e.g. 03 JUN)
 * @param {string} dateString
 */
export function formatDate(dateString) {
    if (!dateString) return { day: '--', month: '---' };
    const date = new Date(dateString);
    return {
        day: String(date.getDate()).padStart(2, '0'),
        month: date.toLocaleDateString('en-GB', { month: 'short' }).toUpperCase()
    };
}

/**
 * Format date for Detail view (e.g. 1st June 2026)
 * @param {string} dateString
 */
export function formatFullDate(dateString) {
    if (!dateString) return 'TBA';
    const date = new Date(dateString);
    const day = date.getDate();
    
    let suffix = 'th';
    if (day % 10 === 1 && day !== 11) suffix = 'st';
    else if (day % 10 === 2 && day !== 12) suffix = 'nd';
    else if (day % 10 === 3 && day !== 13) suffix = 'rd';
    
    const month = date.toLocaleDateString('en-GB', { month: 'short' });
    const year = date.getFullYear();
    
    return `${day}${suffix} ${month} ${year}`;
}
