JaaS local setup (Health Portal Hub)
==================================

Already configured in application.properties:
  App ID: vpaas-magic-cookie-d315eabb12ff4c7e855951f7b571ae6d
  Private key file: jwt.pkcs8.key (auto-generated from jwt.key)

ONE step you must do on https://jaas.8x8.vc/#/apikeys :
  1. Upload jwt.key.pub.pem (drag & drop → Add API Key)
  2. Copy the Key ID (kid) from the keys list
  3. Paste it as the only line in kid.txt (create this file)
  4. Restart Partner-Backend

Files in this folder:
  jwt.key          - your private key (keep secret)
  jwt.key.pub.pem  - upload this to 8x8 JaaS API Keys
  kid.txt          - paste Key ID here after upload (one line, no quotes)
