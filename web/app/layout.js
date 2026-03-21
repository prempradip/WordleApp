export const metadata = {
  title: 'Wordle Unlimited',
  description: 'A free multilingual word puzzle game for Android.',
}

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body style={{ margin: 0, fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif', background: '#f9f9f9', color: '#222' }}>
        {children}
      </body>
    </html>
  )
}
