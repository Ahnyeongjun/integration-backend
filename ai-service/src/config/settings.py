from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application configuration settings"""

    # OpenAI
    openai_api_key: str

    # MySQL
    db_host: str = "localhost"
    db_port: int = 3306
    db_username: str = "root"
    db_password: str
    db_name: str = "wedding_db"

    # Application
    app_host: str = "0.0.0.0"
    app_port: int = 8000

    @property
    def mysql_url(self) -> str:
        return f"mysql+aiomysql://{self.db_username}:{self.db_password}@{self.db_host}:{self.db_port}/{self.db_name}"

    class Config:
        env_file = ".env-ai"
        case_sensitive = False


settings = Settings()
