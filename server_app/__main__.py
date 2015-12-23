from app import app, db, main, socketio
db.create_all()
app.register_blueprint(main)
socketio.run(app, host="0.0.0.0", port=app.config['PORT'])
