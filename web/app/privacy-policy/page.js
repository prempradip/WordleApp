import Link from 'next/link'

export const metadata = {
  title: 'Privacy Policy – Wordle Unlimited',
}

export default function PrivacyPolicy() {
  return (
    <main>
      <header style={styles.header}>
        <h1 style={styles.title}>WORDLE UNLIMITED</h1>
        <p style={styles.subtitle}>Privacy Policy</p>
      </header>

      <article style={styles.article}>
        <span style={styles.badge}>Last updated: March 17, 2026</span>

        <Section title="Overview">
          <p>Thank you for playing <strong>Wordle Unlimited</strong>. Your privacy matters. This policy explains what information the app collects (spoiler: almost none), how it is used, and your rights as a user.</p>
        </Section>

        <Section title="1. Information We Collect">
          <p><strong>Wordle Unlimited does not collect, transmit, or share any personally identifiable information.</strong></p>
          <p style={{ marginTop: 12 }}>The only data the app stores is saved <em>locally on your device</em>:</p>
          <ul>
            <li>Game statistics (wins, losses, streaks, guess distribution)</li>
            <li>Achievements you have unlocked</li>
            <li>Your preferences (theme, language, difficulty, accessibility settings)</li>
          </ul>
          <p style={{ marginTop: 12 }}>This data never leaves your device and is not accessible to us or any third party.</p>
        </Section>

        <Section title="2. Internet Access">
          <p>The app requests the <code>INTERNET</code> permission solely to fetch optional word definitions from a public dictionary API after you complete a puzzle. No personal data, device identifiers, or usage analytics are sent during these requests. If you are offline, this feature is simply skipped.</p>
        </Section>

        <Section title="3. Notifications">
          <p>Wordle Unlimited may send a daily local notification reminding you to play today's puzzle. These notifications are scheduled entirely on-device using Android's alarm system. No push token or remote server is involved.</p>
        </Section>

        <Section title="4. Third-Party Services">
          <p>Wordle Unlimited does <strong>not</strong> integrate any of the following:</p>
          <ul>
            <li>Analytics SDKs (e.g. Firebase Analytics, Mixpanel)</li>
            <li>Advertising networks</li>
            <li>Crash reporting services that transmit data off-device</li>
            <li>Social login or account systems</li>
          </ul>
        </Section>

        <Section title="5. Children's Privacy">
          <p>Wordle Unlimited is suitable for all ages. Because we do not collect any personal information, there is no special risk to children. We do not knowingly collect data from anyone, including children under 13.</p>
        </Section>

        <Section title="6. Data Security">
          <p>All app data is stored in Android's sandboxed local storage (Room database and DataStore). It is protected by the standard Android application sandbox and is not accessible to other apps or external parties.</p>
        </Section>

        <Section title="7. Changes to This Policy">
          <p>If we ever update this privacy policy, the new version will be posted at this URL with an updated "Last updated" date. Continued use of the app after any changes constitutes acceptance of the revised policy.</p>
        </Section>

        <Section title="8. Contact Us">
          <p>If you have any questions or concerns about this privacy policy, please reach out at:<br /><br />
            <a href="mailto:prempradeep@Live.com" style={{ color: '#538d4e' }}>prempradeep@Live.com</a>
          </p>
        </Section>

        <div style={{ marginTop: 32 }}>
          <Link href="/" style={{ color: '#538d4e', fontSize: '0.9rem' }}>← Back to home</Link>
        </div>
      </article>

      <footer style={styles.footer}>
        © 2026 Wordle Unlimited &nbsp;·&nbsp;
        <a href="mailto:prempradeep@Live.com" style={{ color: '#538d4e' }}>prempradeep@Live.com</a>
      </footer>
    </main>
  )
}

function Section({ title, children }) {
  return (
    <div style={sectionStyles.wrap}>
      <h2 style={sectionStyles.heading}>{title}</h2>
      <div style={sectionStyles.card}>{children}</div>
    </div>
  )
}

const styles = {
  header: { background: '#121213', color: '#fff', textAlign: 'center', padding: '40px 24px 32px' },
  title: { fontSize: '2rem', letterSpacing: '4px', margin: '0 0 6px' },
  subtitle: { color: '#aaa', fontSize: '0.95rem', margin: 0 },
  badge: {
    display: 'inline-block', background: '#538d4e', color: '#fff',
    fontSize: '0.78rem', fontWeight: 700, padding: '3px 10px',
    borderRadius: '20px', marginBottom: '16px', letterSpacing: '1px',
  },
  article: { maxWidth: '760px', margin: '40px auto', padding: '0 24px 60px' },
  footer: { textAlign: 'center', fontSize: '0.82rem', color: '#999', padding: '24px', borderTop: '1px solid #e5e5e5' },
}

const sectionStyles = {
  wrap: { marginTop: 8 },
  heading: { fontSize: '1rem', margin: '28px 0 8px', color: '#538d4e', textTransform: 'uppercase', letterSpacing: '1px' },
  card: {
    background: '#fff', borderRadius: '12px', padding: '20px 24px',
    boxShadow: '0 2px 12px rgba(0,0,0,0.07)', fontSize: '0.95rem', lineHeight: 1.7, color: '#444',
  },
}
