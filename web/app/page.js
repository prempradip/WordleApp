import Link from 'next/link'

export default function Home() {
  return (
    <main>
      <header style={styles.header}>
        <h1 style={styles.title}>WORDLE UNLIMITED</h1>
        <p style={styles.subtitle}>A free multilingual word puzzle game</p>
        <a
          href="https://play.google.com/store/apps/details?id=com.prempradip.wordleapp"
          style={styles.badge}
          target="_blank"
          rel="noopener noreferrer"
        >
          ▶ Get it on Google Play
        </a>
      </header>

      <section style={styles.section}>
        <div style={styles.grid}>
          <FeatureCard emoji="🌍" title="4 Languages" desc="Play in English, Spanish, French, or German." />
          <FeatureCard emoji="🎯" title="3 Difficulty Levels" desc="Easy, Normal, and Hard mode with hints." />
          <FeatureCard emoji="📅" title="Daily Word" desc="One shared puzzle every day, plus unlimited practice." />
          <FeatureCard emoji="🏆" title="Achievements" desc="14 achievements to unlock as you improve." />
          <FeatureCard emoji="📖" title="Word Definitions" desc="Learn the meaning of every word you solve." />
          <FeatureCard emoji="🎨" title="5 Color Themes" desc="Classic, AMOLED, Solarized, Pastel, and Ocean." />
        </div>
      </section>

      <footer style={styles.footer}>
        <Link href="/privacy-policy" style={{ color: '#538d4e' }}>Privacy Policy</Link>
        &nbsp;·&nbsp;
        <a href="mailto:prempradeep@Live.com" style={{ color: '#538d4e' }}>Contact</a>
        &nbsp;·&nbsp;© 2026 Wordle Unlimited
      </footer>
    </main>
  )
}

function FeatureCard({ emoji, title, desc }) {
  return (
    <div style={styles.card}>
      <span style={{ fontSize: '2rem' }}>{emoji}</span>
      <h3 style={{ margin: '10px 0 6px', fontSize: '1rem' }}>{title}</h3>
      <p style={{ fontSize: '0.88rem', color: '#666', lineHeight: 1.5 }}>{desc}</p>
    </div>
  )
}

const styles = {
  header: {
    background: '#121213',
    color: '#fff',
    textAlign: 'center',
    padding: '60px 24px 48px',
  },
  title: {
    fontSize: '2.4rem',
    letterSpacing: '6px',
    margin: '0 0 10px',
  },
  subtitle: {
    color: '#aaa',
    fontSize: '1rem',
    margin: '0 0 28px',
  },
  badge: {
    display: 'inline-block',
    background: '#538d4e',
    color: '#fff',
    padding: '12px 28px',
    borderRadius: '30px',
    fontWeight: 700,
    fontSize: '0.95rem',
    textDecoration: 'none',
    letterSpacing: '0.5px',
  },
  section: {
    maxWidth: '900px',
    margin: '48px auto',
    padding: '0 24px',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: '20px',
  },
  card: {
    background: '#fff',
    borderRadius: '14px',
    padding: '24px 20px',
    boxShadow: '0 2px 12px rgba(0,0,0,0.07)',
    textAlign: 'center',
  },
  footer: {
    textAlign: 'center',
    fontSize: '0.85rem',
    color: '#999',
    padding: '24px',
    borderTop: '1px solid #e5e5e5',
  },
}
