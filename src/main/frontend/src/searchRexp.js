function ch2pattern(ch) {
    //영문일 경우
    if (/[a-zA-Z]/.test(ch)) {
        return `[${ch.toLowerCase()}${ch.toUpperCase()}]`;
    }
    const offset = 44032; /* '가'의 코드 */
    // 한국어 음절
    if (/[가-힣]/.test(ch)) {
        const chCode = ch.charCodeAt(0) - offset;
        // 종성이 있으면 문자 그대로를 찾는다.
        if (chCode % 28 > 0) {
            return ch;
        }
        //위 if문으로 인해 28로 나누면 0인 경우만 해당 라인으로 넘어온다.
        //따라서 Math.floor가 필요없을 수 있지만, 부동 소수점으로 인해 만약을 대비해서 붙였다.
        //chCode + offset으로 써도 결과는 똑같이 나오긴 하다.
        const begin = Math.floor(chCode / 28) * 28 + offset;
        const end = begin + 27; // 종성 28가지 경우 다 포함
        return `[\\u${begin.toString(16)}-\\u${end.toString(16)}]`;
    }
    // 한글 자음
    if (/[ㄱ-ㅎ]/.test(ch)) {
        const exceptWords = {
            'ㄱ': '가'.charCodeAt(0),
            'ㄲ': '까'.charCodeAt(0),
            'ㄴ': '나'.charCodeAt(0),
            'ㄷ': '다'.charCodeAt(0),
            'ㄸ': '따'.charCodeAt(0),
            'ㄹ': '라'.charCodeAt(0),
            'ㅁ': '마'.charCodeAt(0),
            'ㅂ': '바'.charCodeAt(0),
            'ㅃ': '빠'.charCodeAt(0),
            'ㅅ': '사'.charCodeAt(0),
        };
        const begin = exceptWords[ch] || ( ( ch.charCodeAt(0) - 12613 /* 'ㅅ'의 코드 */ ) * 588 + exceptWords['ㅅ'] );
        const end = begin + 587; // 중성(21) * 종성(28) = 588의 경우를 합쳤다.
        return `[${ch}\\u${begin.toString(16)}-\\u${end.toString(16)}]`;
    }
    // 그 외엔 그대로 내보냄
    return ch;
}
export function createFuzzyMatcher(input) {
    const pattern = input.split('').map(ch2pattern).join('.*?');
    return new RegExp(pattern);
}

