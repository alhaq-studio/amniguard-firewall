import glob, os, sys

data = {
    'PLAY.md': {
        'title': 'AmniGuard: No-Root Firewall', # 27
        'short': 'Private no-root firewall to block apps, filter domains and save mobile data.' # 76
    },
    'PLAY-en-GB.md': {
        'title': 'AmniGuard: No-Root Firewall', # 27
        'short': 'Private no-root firewall to block apps, filter domains and save mobile data.' # 76
    },
    'PLAY-ar.md': {
        'title': 'AmniGuard: جدار حماية بلا روت', # 29
        'short': 'تحكم بوصول التطبيقات للإنترنت واحجب المواقع الضارة دون روت وبخصوصية تامة.' # 72
    },
    'PLAY-bn.md': {
        'title': 'AmniGuard: নো-রুট ফায়ারওয়াল', # 29
        'short': 'রুট ছাড়াই অ্যাপের ইন্টারনেট নিয়ন্ত্রণ, ডোমেইন ব্লক ও ডেটা সাশ্রয় করুন।' # 71
    },
    'PLAY-de.md': {
        'title': 'AmniGuard: No-Root Firewall', # 27
        'short': 'Internet pro App sperren, Domains filtern & Daten sparen – 100% lokal.' # 70
    },
    'PLAY-es.md': {
        'title': 'AmniGuard: Cortafuegos No Root', # 30
        'short': 'Bloquea Internet por app y filtra dominios sin root. 100% privado y local.' # 74
    },
    'PLAY-fr.md': {
        'title': 'AmniGuard: Pare-feu Sans Root', # 29
        'short': 'Bloquez Internet par app, filtrez les domaines & économisez vos données.' # 73
    },
    'PLAY-hi.md': {
        'title': 'AmniGuard: नो-रूट फायरवॉल', # 25
        'short': 'प्रति ऐप इंटरनेट ब्लॉक करें, डोमेन फ़िल्टर करें और डेटा व बैटरी बचाएं।' # 70
    },
    'PLAY-id.md': {
        'title': 'AmniGuard: Firewall No Root', # 27
        'short': 'Blokir internet per aplikasi, saring domain & hemat data tanpa root.' # 68
    },
    'PLAY-ro.md': {
        'title': 'AmniGuard: Firewall Fără Root', # 29
        'short': 'Blocați accesul la internet per aplicație, filtrați domenii și date.' # 68
    },
    'PLAY-ru.md': {
        'title': 'AmniGuard: Фаервол Без Root', # 27
        'short': 'Блокируйте интернет приложениям, фильтруйте домены и экономьте трафик.' # 71
    },
    'PLAY-ta.md': {
        'title': 'AmniGuard: நோ-ரூட் ஃபயர்வால்', # 28
        'short': 'செயலியின் இணையத்தை கட்டுப்படுத்துங்கள்; ரூட் இன்றி பேட்டரி சேமிக்கவும்.' # 69
    },
    'PLAY-tr.md': {
        'title': 'AmniGuard: Rootsuz Güvenlik', # 27
        'short': 'Uygulama bazlı interneti engelleyin, alan adı filtreleyin. %100 yerel.' # 70
    },
    'PLAY-ur.md': {
        'title': 'AmniGuard: نو روٹ فائر وال', # 26
        'short': 'ہر ایپ کا انٹرنیٹ کنٹرول کریں، سائٹس بلاک اور بغیر روٹ ڈیٹا بچائیں۔' # 66
    }
}

dir_path = 'd:/PROJECTS/Amni/AmniGuard-FireWall/playstore'
for fname, item in data.items():
    fpath = os.path.join(dir_path, fname)
    if os.path.exists(fpath):
        with open(fpath, 'r', encoding='utf-8') as fp:
            content = fp.read()
            
        # Re-format header
        lines = content.split('\n')
        # find title header index and short desc index
        title_idx = -1
        short_idx = -1
        for i, l in enumerate(lines):
            if any(k in l for k in ['App Name', 'اسم التطبيق', 'App-Name', 'Nombre de la', 'Nom de l', 'ऐप का नाम', 'Nama Aplikasi', 'Numele aplica', 'Название прилож', 'பயன்பாட்டின்', 'Uygulama Ad', 'ایپ کا نام', 'অ্যাপের নাম']):
                title_idx = i
            if any(k in l for k in ['Short Description', 'الوصف القصير', 'Kurzbeschreibung', 'Descripción corta', 'Description courte', 'संक्षिप्त विवरण', 'Deskripsi Singkat', 'Descriere scurt', 'Краткое описание', 'குறுகிய விளக்கம்', 'Kısa Açıklama', 'مختصر تفصیل', 'সংক্ষিপ্ত বিবরণ']):
                short_idx = i
                
        if title_idx != -1 and short_idx != -1:
            # Reconstruct neatly
            header_title = lines[title_idx].split('(')[0].strip() + f" ({len(item['title'])} / 30)"
            lines[title_idx] = header_title
            # Find next non-empty line after title_idx to replace title text
            for j in range(title_idx + 1, short_idx):
                if lines[j].strip():
                    lines[j] = item['title']
                    break
                    
            header_short = lines[short_idx].split('(')[0].strip() + f" ({len(item['short'])} / 80)"
            lines[short_idx] = header_short
            for j in range(short_idx + 1, len(lines)):
                if lines[j].startswith('##') or lines[j].startswith('---'):
                    break
                if lines[j].strip():
                    lines[j] = item['short']
                    break
                    
            with open(fpath, 'w', encoding='utf-8') as fp:
                fp.write('\n'.join(lines))

print("Successfully synchronized all listings!")
