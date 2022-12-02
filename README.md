# MathOnline
自由課題で作っているサーバー/クライアントモデルのシンプルな足し算ゲーム

## 使い方
`java -jar MathOnline.jar --help` でヘルプを表示。

## サーバー起動
Dockerディレクトリで `docker-compose up` を実行 or `java -jar MathOnline.jar -s` を実行

## サーバーに接続
`java -jar MathOnline.jar` を実行して起動したら 2 を選択してサーバーのIPを入力

## デバッグモード
`java -jar MathOnline.jar -d` と実行するとデバッグモードの状態で起動できる。

バナーが表示されなかったらデバッグモードで起動できている。

デバッグモードでサーバーに接続すると **Debug Options** というオプションが追加される。 (※サーバーもデバッグモードで起動してある必要がある。)

### デバッグオプション一覧
- Auto Play | 自動で問題を解いてくれる。