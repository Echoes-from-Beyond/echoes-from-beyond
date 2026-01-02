# Security Policy

## Supported Versions

Security reports are supported for the latest patch of every released major.minor.patch version.

For example, if versions `4.7.6`, `4.8.7` and `5.1.3` are released, this repository accepts reports on `4.7.6`, `4.8.7`, and `5.1.3`, but would not accept reports on `5.1.2` or `5.1.0`.

## Reporting a Vulnerability

Email <chemky2000@gmail.com> to report vulnerabilities. The title of the email should contain the text `VULNERABILITY REPORT` somewhere.

Reports must use the format detailed [here](#format). Please copy-paste this into your email client of choice. It's also strongly recommended to _encrypt_ the contents using PGP, with the following key:

<details>
  <summary>Public Key</summary>

  ```
-----BEGIN PGP PUBLIC KEY BLOCK-----

mDMEaVc//hYJKwYBBAHaRw8BAQdAEhs9RbWoUnn9J+CBtbA8jJZ3KyrR0VbKaYS/
CC3xvo+0Nkt5bGUgUHJld2l0dCAoRW1haWwgRW5jcnlwdGlvbikgPGNoZW1reTIw
MDBAZ21haWwuY29tPoi1BBMWCgBdFiEEYpwkHpN5UtNLN2oPem43InaIFdQFAmlX
P/4bFIAAAAAABAAObWFudTIsMi41KzEuMTEsMiwyAhsDBQkB4TOABQsJCAcCAiIC
BhUKCQgLAgQWAgMBAh4HAheAAAoJEHpuNyJ2iBXUKbsBAINo1X7IKv1OftLvHXkl
LdGWosvggGIiGhkzSLpYJgqnAPkBoNofwpOAAS1QedAQAbH2lTTk8MLAXSgy57bT
Ogk7B7g4BGlXP/4SCisGAQQBl1UBBQEBB0DgpaYIjU8DPTvC80T++upV2rNgQknD
j/5tmqqWvh2yFgMBCAeImgQYFgoAQhYhBGKcJB6TeVLTSzdqD3puNyJ2iBXUBQJp
Vz/+GxSAAAAAAAQADm1hbnUyLDIuNSsxLjExLDIsMgIbDAUJAeEzgAAKCRB6bjci
dogV1ALVAQDe7ZVgYwUnjetZsLBbltc5RP3+AoPOjxCZYn4rlhRZeAEAhhA0hEZx
ahoR0OYs+aZK3dNSYNzgBpmEZH9iB2G7ZAo=
=DuFG
-----END PGP PUBLIC KEY BLOCK-----
  ```
</details>

We will respond to reports within one week. We will follow up for more information if the report isn't actionable as-is.

Once a reported vulnerability has been resolved and a fix is available, we will publicly announce it through all relevant channels.

Unfortunately, we cannot afford to pay bounties. We will **ignore all vulnerability reports that demand payment for disclosure of some hypothetical vulnerability.**

### Format
`OPTIONAL` entries may be left off as needed.

```
Impact: [a short description of what the vulnerability entails]

Commit: [earliest commit hash where the vulnerability is present]

Locations: [OPTIONAL: a list of source code file paths, relative to
    the root of the repository, and relevant line numbers if possible]
    
Reproducer: [OPTIONAL: if present, explain how to exploit the 
    vulnerability, including relevant code samples, etc]

Key: [OPTIONAL: an ASCII-armored PGP key which we will use to encrypt
    all followup emails to your report]
```
